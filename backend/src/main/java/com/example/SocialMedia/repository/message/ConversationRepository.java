package com.example.SocialMedia.repository.message;

import com.example.SocialMedia.dto.projection.ConversationProjection;
import com.example.SocialMedia.model.messaging_model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    // 1. Gọi SP lấy danh sách hội thoại
    // Lưu ý: SQL Server của bạn đang dùng OFFSET (@PageNumber - 1), nên truyền PageNumber bắt đầu từ 1
    @Query(value = "EXEC Messaging.SP_GetUserConversations :userId, :pageSize, :pageNumber", nativeQuery = true)
    List<ConversationProjection> getUserConversations(
            @Param("userId") Integer userId,
            @Param("pageSize") Integer pageSize,
            @Param("pageNumber") Integer pageNumber
    );

    // 2. Gọi SP đánh dấu đã đọc (Vì đây là lệnh Update, cần @Modifying)
    @Modifying
    @Query(value = "EXEC Messaging.SP_MarkMessagesAsRead :userId, :conversationId, :lastMessageId", nativeQuery = true)
    void markMessagesAsRead(
            @Param("userId") Integer userId,
            @Param("conversationId") Integer conversationId,
            @Param("lastMessageId") Long lastMessageId
    );

    // 3. Gọi SP đếm số tin chưa đọc (cho trường hợp cần check nhanh)
    @Query(value = "EXEC Messaging.SP_GetUnreadMessageCount :userId, :conversationId", nativeQuery = true)
    Integer getUnreadMessageCount(
            @Param("userId") Integer userId,
            @Param("conversationId") Integer conversationId
    );
}