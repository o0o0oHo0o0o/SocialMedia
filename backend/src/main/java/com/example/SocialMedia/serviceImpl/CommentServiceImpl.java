package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.CommentRequest;
import com.example.SocialMedia.dto.CommentResponse;
import com.example.SocialMedia.dto.ShortUserResponse;
import com.example.SocialMedia.exception.ResourceNotFound.CommentNotFoundException;
import com.example.SocialMedia.exception.ResourceNotFound.PostNotFoundException;
import com.example.SocialMedia.exception.ResourceNotFound.UserNotFoundException;
import com.example.SocialMedia.model.coredata_model.Comment;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.repository.CommentRepository;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.repository.ReactionRepository;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.CommentService;
import com.example.SocialMedia.service.InteractableItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final InteractableItemService interactableItemService;
    private final ReactionRepository reactionRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              UserRepository userRepository,
                              PostRepository postRepository,
                              InteractableItemService interactableItemService, ReactionRepository reactionRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.interactableItemService = interactableItemService;
        this.reactionRepository = reactionRepository;
    }
    private CommentResponse convertToSingleCommentResponse(Comment comment){
        return new CommentResponse.PostMediaResponseBuilder()
                .id(comment.getCommentId())
                .content(comment.getContent())
                .user(new ShortUserResponse(comment.getUser().getId(), comment.getUser().getUsername()))
                .interactableItemId(comment.getOwnInteractableItem().getInteractableItemId())
                .parentCommentId(comment.getParentComment() != null
                        ? comment.getParentComment().getCommentId()
                        : null)
                .replied(commentRepository.existsByParentComment_CommentId(comment.getCommentId()))
                .reaction(reactionRepository.countReactionsByInteractableItemId(comment.getOwnInteractableItem().getInteractableItemId()))
                .createdAt(comment.getCreatedLocalDateTime())
                .build();
    }
    private List<CommentResponse> convertToCommentResponse(List<Comment> comments) {
        return comments.stream()
                .map(this::convertToSingleCommentResponse)
                .toList();
    }
    public List<CommentResponse> getCommentsByPostId(Integer id, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPost_InteractableItem_InteractableItemId_AndParentCommentIsNull(id, pageable);
        return convertToCommentResponse(comments.getContent());
    }
    public List<CommentResponse> getCommentsByUserId(Integer userId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByUserId(userId, pageable);
        return comments.stream()
                .map(comment -> new CommentResponse.PostMediaResponseBuilder()
                        .id(comment.getCommentId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedLocalDateTime())
                        .build())
                .toList();
    }
    public List<CommentResponse> getCommentsByParentCommentId(Integer parentCommentId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByParentComment_CommentId(parentCommentId, pageable);
        return convertToCommentResponse(comments.getContent());
    }
    public CommentResponse createComment(CommentRequest commentRequest) {
        User user = userRepository.findById(commentRequest.getUserId())
                .orElseThrow(()->new UserNotFoundException("User not found"+commentRequest.getUserId()));
        Post post = postRepository.findByInteractableItem_InteractableItemId(commentRequest.getTargetInteractableItemID())
                .orElseThrow(()->new PostNotFoundException("Post not found"+commentRequest.getTargetInteractableItemID()));

        Comment comment = new Comment();
        if (commentRequest.getId() != null) {
            comment.setCommentId(commentRequest.getId());
        }
        comment.setUser(user);
        comment.setPost(post);
        if (commentRequest.getParentCommentId() != null){
            Comment parent = commentRepository.findByCommentId(commentRequest.getParentCommentId())
                    .orElseThrow(()->new CommentNotFoundException("Comment parent not found"+commentRequest.getParentCommentId()));
            comment.setParentComment(parent);
        }
        comment.setContent(commentRequest.getContent());
        comment.setOwnInteractableItem(interactableItemService.createInteractableItems("COMMENT", commentRequest.getCreatedAt()));
        comment.setCreatedLocalDateTime(commentRequest.getCreatedAt());
        comment = commentRepository.save(comment);
        return convertToSingleCommentResponse(comment);
    }

    public CommentResponse deleteComment(Integer commentId) {
        Comment comment = commentRepository.findByCommentId(commentId)
                .orElseThrow(()->new CommentNotFoundException("Comment not found"+commentId));
        comment.setDeleted(true);
        comment = commentRepository.save(comment);
        return new CommentResponse.PostMediaResponseBuilder()
                .id(comment.getCommentId())
                .content(comment.getContent())
                .user(new ShortUserResponse(comment.getUser().getId(), comment.getUser().getUsername()))
                .build();
    }
}
