package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.CommentRequest;
import com.example.SocialMedia.dto.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    List<CommentResponse> getCommentsByPostId(Integer id, Pageable pageable);
    List<CommentResponse> getCommentsByUserId(Integer userId, Pageable pageable);
    List<CommentResponse> getCommentsByParentCommentId(Integer parentCommentId, Pageable pageable);
    CommentResponse createComment(CommentRequest commentRequest);
    CommentResponse deleteComment(Integer commentId);
}
