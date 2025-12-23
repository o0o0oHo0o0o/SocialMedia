package com.example.SocialMedia.serviceImpl.social;

import com.example.SocialMedia.dto.request.CommentRequest;
import com.example.SocialMedia.dto.response.CommentResponse;
import com.example.SocialMedia.dto.response.ReactionStat; // Import Interface này
import com.example.SocialMedia.dto.response.ShortUserResponse;
import com.example.SocialMedia.exception.ResourceNotFound.CommentNotFoundException;
import com.example.SocialMedia.exception.ResourceNotFound.PostNotFoundException;
import com.example.SocialMedia.exception.ResourceNotFound.UserNotFoundException;
import com.example.SocialMedia.model.coredata_model.*;
import com.example.SocialMedia.repository.*;
import com.example.SocialMedia.service.social.CommentService;
import com.example.SocialMedia.service.social.InteractableItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final InteractableItemService interactableItemService;
    private final ReactionRepository reactionRepository;

    // --- HELPER CONVERT ---
    private CommentResponse convertToSingleCommentResponse(Comment comment){
        // 1. Lấy thống kê reaction (Fix lỗi Incompatible types)
        List<ReactionStat> stats = reactionRepository.countReactionsByInteractableItemId(
                comment.getOwnInteractableItem().getInteractableItemId()
        );

        // 2. Convert List Stat -> Map
        Map<String, Long> reactionMap = stats.stream()
                .collect(Collectors.toMap(
                        stat -> stat.getReactionType().name(),
                        ReactionStat::getReactionCount
                ));

        // 3. Sử dụng Builder của Lombok (Đã sửa tên đúng)
        return CommentResponse.builder()
                .id(comment.getCommentId())
                .content(comment.getContent())
                .user(new ShortUserResponse(
                        comment.getUser().getId(),
                        comment.getUser().getFullName(),
                        comment.getUser().getUsername(),
                        comment.getUser().getProfilePictureURL(),
                        comment.getUser().getCreatedLocalDateTime()))         // Assuming `getAge()` exists
                .interactableItemId(comment.getOwnInteractableItem().getInteractableItemId())
                .targetInteractableItemId(comment.getPost().getInteractableItem().getInteractableItemId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null)
                // Lưu ý: existsBy... trong vòng lặp sẽ gây N+1 query (Tạm chấp nhận nếu app nhỏ)
                .replied(commentRepository.existsByParentComment_CommentId(comment.getCommentId()))
                .reactionCounts(reactionMap) // Truyền Map vào đây
                .createdAt(comment.getCreatedLocalDateTime())
                .build();
    }

    private List<CommentResponse> convertToCommentResponse(List<Comment> comments) {
        return comments.stream()
                .map(this::convertToSingleCommentResponse)
                .toList();
    }

    // --- MAIN METHODS ---

    @Override
    public List<CommentResponse> getCommentsByPostId(Integer id, Pageable pageable) {
        // Query comment gốc (không có parent) của bài viết
        Page<Comment> comments = commentRepository.findByPost_InteractableItem_InteractableItemId_AndParentCommentIsNull(id, pageable);
        return convertToCommentResponse(comments.getContent());
    }

    @Override
    public List<CommentResponse> getCommentsByUserName(String userName, Pageable pageable) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userName));
        Page<Comment> comments = commentRepository.findByUserAndIsDeletedIsFalse(user, pageable);
        return convertToCommentResponse(comments.getContent());
    }

    @Override
    public List<CommentResponse> getCommentsByParentCommentId(Integer parentCommentId, Pageable pageable) {
        // Lấy danh sách trả lời (Replies)
        Page<Comment> comments = commentRepository.findByParentComment_CommentId(parentCommentId, pageable);
        return convertToCommentResponse(comments.getContent());
    }

    @Override
    @Transactional
    public CommentResponse createComment(String username, CommentRequest request) { // Thêm tham số username
        // 1. Lấy User từ Token
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        // 2. Tìm Post
        Post post = postRepository.findByInteractableItem_InteractableItemId(request.getTargetInteractableItemID())
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(request.getContent());
        comment.setCreatedLocalDateTime(request.getCreatedAt());

        // 3. Xử lý Parent Comment (nếu là reply)
        if (request.getParentCommentId() != null){
            Comment parent = commentRepository.findByCommentId(request.getParentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        // 4. Tạo InteractableItem riêng cho Comment (để Comment cũng có thể được Like)
        comment.setOwnInteractableItem(
                interactableItemService.createInteractableItems("COMMENT", request.getCreatedAt())
        );

        comment = commentRepository.save(comment);
        return convertToSingleCommentResponse(comment);
    }

    @Override
    public CommentResponse deleteComment(Integer commentId) {
        Comment comment = commentRepository.findByCommentId(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        comment.setDeleted(true); // Soft Delete
        comment = commentRepository.save(comment);

        // Trả về response đơn giản
        return convertToSingleCommentResponse(comment);
    }
}