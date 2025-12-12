package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.Comment;
import com.example.SocialMedia.model.coredata_model.InteractableItems;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    Optional<Comment> findByCommentId(int commentId);
    int countCommentByPost(Post post);
    Page<Comment> findByPost_InteractableItem_InteractableItemId_AndParentCommentIsNull(Integer postId, Pageable pageable);
    Page<Comment> findByUserId(Integer userId, Pageable pageable);

    Page<Comment> findByParentComment_CommentId(Integer parentCommentId, Pageable pageable);
    boolean existsByParentComment_CommentId(Integer parentCommentId);
    Optional<Comment> findByOwnInteractableItem_InteractableItemId(Integer interactableItemId);
}
