package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.PostMediaResponse;
import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.model.coredata_model.*;
import com.example.SocialMedia.repository.*;
import com.example.SocialMedia.service.InteractableItemService;
import com.example.SocialMedia.service.PostMediaService;
import com.example.SocialMedia.service.PostService;
import io.jsonwebtoken.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMediaRepository postMediaRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final InteractableItemService interactableItemService;
    private final PostMediaService postMediaService;
    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           PostMediaRepository postMediaRepository,
                           ReactionRepository reactionRepository,
                           CommentRepository commentRepository,
                           ShareRepository shareRepository,
                           InteractableItemService interactableItemService,
                           PostMediaService postMediaService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postMediaRepository = postMediaRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.shareRepository = shareRepository;
        this.interactableItemService = interactableItemService;
        this.postMediaService = postMediaService;
    }

    private void createMultiMedia(MultipartFile[] medias, Post post) throws IOException {
        if (!(medias != null && medias.length > 0)) {
            return;
        }
        for (MultipartFile media : medias){
            try {
                postMediaService.createPostMedia(media, post);
            } catch (java.io.IOException e) {
                // Handle the exception: log, rethrow, or return an error message
                System.err.println("Error while creating post media: " + e.getMessage());
                // Optionally, throw a runtime exception if you want to fail the operation
                throw new RuntimeException("Error while handling media file upload", e);
            }
        }
    }
    private void deleteMultiMedia(int[] ids) throws IOException {
        if (!(ids != null && ids.length > 0)) {
            return;
        }
        for (int mediaId : ids){
            PostMedia media = postMediaRepository.findPostMediaByPostMediaId(mediaId);
            postMediaService.deletePostMedia(media);
        }
    }
    @Override
    public PostResponse getPostById(Integer id) {
        Post post = postRepository.findByPostId(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        PostResponse response = new PostResponse();
        response.setId(post.getPostId());
        response.setContent(post.getContent());
        response.setPostTopic(post.getPostTopic());
        response.setLocation(post.getLocation());
        response.setUsername(post.getUser().getUsername());  // Assuming 'User' has 'username'
        response.setCreatedAt(post.getCreatedLocalDateTime());
        response.setUpdatedAt(post.getUpdatedLocalDateTime());

        List<PostMedia> medias = postMediaRepository.findByPost(post);
        List<PostMediaResponse> mediasResponse = new ArrayList<>();
        for (PostMedia media : medias) {
            PostMediaResponse mediaResponse = new PostMediaResponse();
            mediaResponse.setId(media.getPostMediaId());
            mediaResponse.setMediaURL(media.getMediaURL());
            mediaResponse.setMediaType(media.getMediaType());
            mediaResponse.setSortOrder(media.getSortOrder());
            mediasResponse.add(mediaResponse);
        }
        response.setMedias(mediasResponse);
        response.setCommentCount(commentRepository.countCommentByPost(post));
        response.setShareCount(shareRepository.countShareByPost(post));
        response.setReactionCount(reactionRepository.countReactionByInteractableItems(post.getInteractableItem()));
        return response;
    }

    @Override
    public PostResponse createPost(PostRequest postRequest){
        // Find user by username (if username exists)
        User user = userRepository.findById(postRequest.getUserID())
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        return getPostById(post.getPostId());
    }

    @Override
    public List<PostResponse> getPostByUserId(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<PostResponse> postResponses = new ArrayList<>();
        List<Post> posts = postRepository.findByUser(user);
        for (Post post : posts) {
            postResponses.add(getPostById(post.getPostId()));
        }
        return postResponses;
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
        postRepository.save(post);

        createMultiMedia(postRequest.getMedias(), post);
        deleteMultiMedia(postRequest.getDeleteMedia());
        return getPostById(post.getPostId());
    }

    @Override
    public void deletePost(int postId) {
        // Check if the post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        post.setDeleted(true);
        // Delete the post
        List<PostMedia> medias = postMediaRepository.findByPost(post);
        // Delete post media
        for (PostMedia media : medias) {
            postMediaService.deletePostMedia(media);
        }
        postRepository.save(post);
    }
}
