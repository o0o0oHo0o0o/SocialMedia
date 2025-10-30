package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.PostMediaResponse;
import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.model.coredata_model.*;
import com.example.SocialMedia.repository.*;
import com.example.SocialMedia.service.PostService;
import com.example.SocialMedia.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final InteractableItemsRepository interactableItemsRepository;
    private final UserRepository userRepository;
    private final PostMediaRepository postMediaRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository,
                           InteractableItemsRepository interactableItemsRepository,
                           UserRepository userRepository,
                           PostMediaRepository postMediaRepository,
                           ReactionRepository reactionRepository,
                           CommentRepository commentRepository,
                           ShareRepository shareRepository) {
        this.postRepository = postRepository;
        this.interactableItemsRepository = interactableItemsRepository;
        this.userRepository = userRepository;
        this.postMediaRepository = postMediaRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.shareRepository = shareRepository;
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
    public PostResponse createPost(PostRequest postRequest) {
        // Find user by username (if username exists)
        User user = userRepository.findById(postRequest.getUserID())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find interactable item (assuming you have logic to fetch it)
        InteractableItems item = new InteractableItems();
        item.setItemType("POST");
        item.setCreatedAt(postRequest.getCreatedAt());
        interactableItemsRepository.save(item);

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
        return getPostById(post.getPostId());
    }

    @Override
    public List<PostMedia> findMediaByPost(Post post){
        return postMediaRepository.findByPost(post);
    }

    @Override
    public void deletePost(int postId) {
        // Check if the post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        post.setDeleted(true);
        // Delete the post
        postRepository.save(post);
    }
}
