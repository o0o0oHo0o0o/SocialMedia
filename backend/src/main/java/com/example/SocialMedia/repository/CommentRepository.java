package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.Comment;
import com.example.SocialMedia.model.coredata_model.InteractableItems;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    int countCommentByPost(Post post);
    Page<Comment> findByPost(Post post, Pageable pageable);
    Page<Comment> findByUser(User user, Pageable pageable);

    Page<Comment> findByParentComment(Comment parentComment, Pageable pageable);
}
