package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.exception.ResourceNotFound.PostNotFoundException;
import com.example.SocialMedia.exception.ResourceNotFound.UserNotFoundException;
import com.example.SocialMedia.model.coredata_model.*;
import com.example.SocialMedia.repository.*;
import com.example.SocialMedia.service.InteractableItemService;
import com.example.SocialMedia.service.PostMediaService;
import com.example.SocialMedia.service.PostService;
import com.example.SocialMedia.service.ReactionService;
import io.jsonwebtoken.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final ReactionService reactionService;
    int MAX_LOAD_MEDIA = 4;
    Pageable MEDIA_PAGEABLE = PageRequest.of(0, MAX_LOAD_MEDIA);
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final InteractableItemService interactableItemService;
    private final PostMediaService postMediaService;
    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           ReactionRepository reactionRepository,
                           CommentRepository commentRepository,
                           ShareRepository shareRepository,
                           InteractableItemService interactableItemService,
                           PostMediaService postMediaService, ReactionService reactionService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.shareRepository = shareRepository;
        this.interactableItemService = interactableItemService;
        this.postMediaService = postMediaService;
        this.reactionService = reactionService;
    }

    private void createMultiMedia(MultipartFile[] medias, Post post){
        if (medias != null) {
            for (MultipartFile media : medias) {
                postMediaService.createPostMedia(media, post);
            }
        }
    }
    private void deleteMultiMedia(int[] ids) throws IOException {
        if (ids != null) for (int mediaId : ids) postMediaService.deletePostMedia(mediaId);
    }
    PostResponse convertToPostResponse(Post post){
        return new PostResponse.PostResponseBuilder()
                .id(post.getPostId())
                .content(post.getContent())
                .postTopic(post.getPostTopic())
                .location(post.getLocation())
                .username(post.getUser().getUsername())
                .interactableItemId(post.getInteractableItem().getInteractableItemId())
                .createdAt(post.getCreatedLocalDateTime())
                .updatedAt(post.getUpdatedLocalDateTime())
                .commentCount(commentRepository.countCommentByPost(post))
                .reactionCount(reactionRepository.countReactionsByInteractableItemId(post.getInteractableItem().getInteractableItemId()))
                .shareCount(shareRepository.countShareByPost(post))
                .medias(postMediaService.findByPost(post, MEDIA_PAGEABLE))
                .build();
    }
    @Override
    public PostResponse getPostById(Integer id) {
        Post post = postRepository.findByPostId(id)
                .orElseThrow(()-> new PostNotFoundException("Post not found"));
        return convertToPostResponse(post);
    }

    @Override
    public PostResponse createPost(PostRequest postRequest){
        // Find user by username (if username exists)
        User user = userRepository.findById(postRequest.getUserID())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + postRequest.getUserID()));

        // Create Interactable Item
        InteractableItems item = interactableItemService.createInteractableItems("POST", postRequest.getCreatedAt());

        // Create new Post entity
        Post post = new Post();
        post.setContent(postRequest.getContent());
        post.setPostTopic(postRequest.getPostTopic());
        post.setLocation(postRequest.getLocation());
        post.setUser(user); // Set the user who is creating the post
        post.setInteractableItem(item);  // Set the interactable item
        post.setCreatedLocalDateTime(postRequest.getCreatedAt());  // Set the creation time

        // Save the post using the repository
        post =  postRepository.save(post);

        //Save post media
        createMultiMedia(postRequest.getMedias(), post);

        return convertToPostResponse(post);
    }

    @Override
    public List<PostResponse> getPostByUserId(Integer id, Pageable pageable) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        Page<Post> posts = postRepository.findByUserAndIsDeletedIsFalse(user, pageable);
        return posts.getContent().stream()
                .map(post -> new PostResponse.PostResponseBuilder()
                        .id(post.getPostId())
                        .content(post.getContent())
                        .postTopic(post.getPostTopic())
                        .location(post.getLocation())
                        .username(post.getUser().getUsername())  // Assuming User has getUsername()
                        .reactionCount(reactionRepository.countReactionsByInteractableItemId(post.getInteractableItem().getInteractableItemId()))
                        .commentCount(commentRepository.countCommentByPost(post))
                        .shareCount(shareRepository.countShareByPost(post))
                        .updatedAt(post.getUpdatedLocalDateTime())
                        .createdAt(post.getCreatedLocalDateTime())
                        .medias(postMediaService.findByPost(post, MEDIA_PAGEABLE))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PostResponse updatePost(PostRequest postRequest) {

        // Find the post by ID
        Post post = postRepository.findByPostId(postRequest.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Update only the fields that are not null
        if (postRequest.getContent() != null) {
            post.setContent(postRequest.getContent());
        }
        if (postRequest.getPostTopic() != null) {
            post.setPostTopic(postRequest.getPostTopic());
        }
        if (postRequest.getLocation() != null) {
            post.setLocation(postRequest.getLocation());
        }

        post.setUpdatedLocalDateTime(postRequest.getUpdatedAt());  // Set the creation time

        // Save the post using the repository
        post = postRepository.save(post);

        createMultiMedia(postRequest.getMedias(), post);
        deleteMultiMedia(postRequest.getDeleteMedia());
        return convertToPostResponse(post);
    }

    @Override
    public PostResponse deletePost(int postId) {
        // Check if the post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found with id: " + postId));
        post.setDeleted(true);
        // Delete the post
        postMediaService.deleteByPost(post);
        return convertToPostResponse(postRepository.save(post));
    }
}
