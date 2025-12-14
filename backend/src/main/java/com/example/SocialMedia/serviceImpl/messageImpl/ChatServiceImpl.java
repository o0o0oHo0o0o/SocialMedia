package com.example.SocialMedia.serviceImpl.messageImpl;

import com.example.SocialMedia.dto.message.MessageStatusSummary;
import com.example.SocialMedia.dto.message.UserReadStatus;
import com.example.SocialMedia.dto.projection.ConversationProjection;
import com.example.SocialMedia.dto.request.SendMessageRequest;
import com.example.SocialMedia.dto.response.*;
import com.example.SocialMedia.keys.MessageStatusID;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.model.messaging_model.*;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.repository.message.*;
import com.example.SocialMedia.service.IMinioService;
import com.example.SocialMedia.service.message.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.SocialMedia.model.messaging_model.MessageStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepo;
    private final MessageBodyRepository messageBodyRepo;
    private final MessageMediaRepository messageMediaRepo;
    private final MessageStatusRepository messageStatusRepo;
    private final ConversationRepository conversationRepo;
    private final ConversationMemberRepository conversationMemberRepo;
    private final UserRepository userRepo;
    private final IMinioService minioService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public MessageResponse sendMessage(String username, SendMessageRequest request, List<MultipartFile> files) {
        // 1. Fetch User & Conversation
        User sender = userRepo.findByUserName(username).orElseThrow(() -> new RuntimeException("User not found"));
        Conversation conversation = conversationRepo.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Messages message = new Messages();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setSentAt(LocalDateTime.now());
        message.setMessageType("TEXT");
        long nextSeq = conversation.getLastMessageID() + 1;
        message.setSequenceNumber(nextSeq);

        // FIX: Set replyMessage từ fresh object từ DB
        if (request.getReplyToMessageId() != null) {
            messageRepo.findById(request.getReplyToMessageId()).ifPresent(message::setReplyMessage);
        }

        Messages savedMessage = messageRepo.save(message); // Save để có ID

        // ===== Tạo MessageStatus cho tất cả members =====
        List<ConversationMember> members = conversationMemberRepo
                .findByConversation_ConversationId(conversation.getConversationId());
        int senderId = sender.getId();
        for (ConversationMember member : members) {
            // Không tạo status cho chính người gửi
            if (member.getUser().getId() != senderId) {
                MessageStatus status = new MessageStatus();

                MessageStatusID statusId = new MessageStatusID();
                statusId.setMessageID(savedMessage.getMessageId());
                statusId.setUserID(member.getUser().getId());

                status.setId(statusId);
                status.setMessage(savedMessage);
                status.setUser(member.getUser());
                status.setStatus(MessageStatus.MessageStatusEnum.SENT);

                messageStatusRepo.save(status);
            }
        }
        // ============================================================
        // 3. Handle Message Body (Content text) - FIX: Use native SQL insert
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            try {
                messageBodyRepo.insertBody(savedMessage.getMessageId(), request.getContent());
            } catch (Exception e) {
                // Sửa logging với constant message
                org.slf4j.LoggerFactory.getLogger(this.getClass())
                        .warn("Failed to insert message body", e);
            }
        }

        // 4. Handle Media (MinIO)
        List<MediaDto> mediaDtos = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String storedFileName = minioService.uploadFile(file).getFileName();
                String type = determineMediaType(file.getContentType());

                MessageMedia media = new MessageMedia();
                media.setMessage(savedMessage);
                media.setMediaName(storedFileName);
                media.setMediaType(type);
                media.setFileName(file.getOriginalFilename());
                media.setFileSize((int) file.getSize());
                messageMediaRepo.save(media);

                mediaDtos.add(MediaDto.builder()
                        .url(minioService.getFileUrl(storedFileName))
                        .type(type)
                        .fileName(file.getOriginalFilename())
                        .fileSize((int) file.getSize())
                        .build());
            }
        }

        // 5. Update Conversation Metadata
        conversation.setLastMessageID((int) savedMessage.getMessageId());
        conversationRepo.save(conversation);

        // 6. Get Nickname
        ConversationMember memberInfo = conversationMemberRepo
                .findByConversation_ConversationIdAndUser_Id(conversation.getConversationId(), sender.getId())
                .orElse(null);
        String nickname = memberInfo != null ? memberInfo.getNickname() : null;

        // 7. Build Response
        MessageResponse response = MessageResponse.builder()
                .messageId(savedMessage.getMessageId())
                .sender(SenderDto.fromUser(sender, nickname))
                .conversationId(conversation.getConversationId())
                .content(request.getContent())
                .media(mediaDtos)
                .sender(SenderDto.fromUser(sender, nickname))
                .sentAt(savedMessage.getSentAt().toString())
                .replyToMessageId(request.getReplyToMessageId())
                .isRead(false)
                .isDelivered(false)
                .build();

        // 8. REAL-TIME PUSH (Socket)
        SocketResponse<MessageResponse> socketEvent = SocketResponse.<MessageResponse>builder()
                .type("NEW_MESSAGE")
                .payload(response)
                .build();

        messagingTemplate.convertAndSend("/topic/chat." + conversation.getConversationId(), socketEvent);

        return response;
    }
    public List<ConversationResponse> getUserConversations(String username, int page, int size) {
        User user = userRepo.findByUserName(username).orElseThrow();

        // Gọi SP (Page bắt đầu từ 1)
        List<ConversationProjection> rawData = conversationRepo.getUserConversations(user.getId(), size, page);

        // Map từ Projection sang Response DTO (để xử lý URL ảnh)
        return rawData.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(String username, int conversationId, int page, int size) {
        // 0) Kiểm tra membership
        User user = userRepo.findByUserName(username).orElseThrow();
        boolean isMember = conversationMemberRepo
                .existsByConversation_ConversationIdAndUser_Id(conversationId, user.getId());
        if (!isMember) throw new RuntimeException("Not a conversation member");

        // 1) Lấy page message - DESCENDING để lấy tin mới nhất trước
        int p = Math.max(page - 1, 0);
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(p, size);

        // ✅ SỬA ĐÂY: Dùng method DESC từ repository
        var pageMsgs = messageRepo.findByConversationIdOrderBySequenceNumberDesc(conversationId, pageable);
        List<Messages> messages = pageMsgs.getContent();
        if (messages.isEmpty()) return List.of();

        // 2) Batch data (phần còn lại giữ nguyên)
        List<Long> ids = messages.stream().map(Messages::getMessageId).toList();
        int currentUserId = user.getId();

        var bodiesByMsgId = messageBodyRepo.findByMessageIDIn(ids).stream()
                .collect(java.util.stream.Collectors.groupingBy(MessageBodies::getMessageID));

        var mediaByMsgId = messageMediaRepo.findByMessage_MessageIdIn(ids).stream()
                .collect(java.util.stream.Collectors.groupingBy(m -> m.getMessage().getMessageId()));

        var statusesByMsgId = messageStatusRepo.findByMessage_MessageIdIn(ids).stream()
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getMessage().getMessageId()));

        // 3) Map ra DTO
        List<Long> undeliveredIds = new java.util.ArrayList<>();
        List<MessageResponse> result = messages.stream().map(m -> {
            // Content
            String content = bodiesByMsgId.getOrDefault(m.getMessageId(), java.util.List.of())
                    .stream().findFirst().map(MessageBodies::getContent).orElse(null);

            // Media
            List<MediaDto> mediaDtos = mediaByMsgId.getOrDefault(m.getMessageId(), java.util.List.of()).stream()
                    .map(media -> MediaDto.builder()
                            .url(minioService.getFileUrl(media.getMediaName()))
                            .type(media.getMediaType())
                            .fileName(media.getFileName())
                            .build())
                    .toList();

            // Nickname
            String nickname = conversationMemberRepo
                    .findByConversation_ConversationIdAndUser_Id(m.getConversation().getConversationId(), m.getSender().getId())
                    .map(ConversationMember::getNickname)
                    .orElse(null);

            // Status calculation
            boolean isMySent = m.getSender().getId() == currentUserId;
            List<MessageStatus> statuses = statusesByMsgId.getOrDefault(m.getMessageId(), java.util.List.of());

            MessageStatus relevantStatus = statuses.stream()
                    .filter(s -> isMySent == (s.getUser().getId() != currentUserId))
                    .findFirst()
                    .orElse(null);

            boolean isRead = relevantStatus != null && MessageStatus.MessageStatusEnum.READ.equals(relevantStatus.getStatus());
            boolean isDelivered = relevantStatus != null &&
                    (MessageStatus.MessageStatusEnum.DELIVERED.equals(relevantStatus.getStatus()) || isRead);

            // Collect undelivered IDs - CHỈ cho status SENT
            if (!isMySent && relevantStatus != null &&
                    MessageStatus.MessageStatusEnum.SENT.equals(relevantStatus.getStatus())) {
                undeliveredIds.add(m.getMessageId());
            }

            return MessageResponse.builder()
                    .messageId(m.getMessageId())
                    .conversationId(m.getConversation().getConversationId())
                    .content(content)
                    .media(mediaDtos)
                    .sender(SenderDto.fromUser(m.getSender(), nickname))
                    .sentAt(m.getSentAt() != null ? m.getSentAt().toString() : null)
                    .replyToMessageId(m.getReplyMessage() != null ? m.getReplyMessage().getMessageId() : null)
                    .isRead(isRead)
                    .isDelivered(isDelivered)
                    .statusSummary(buildStatusSummary(statuses, currentUserId))
                    .build();
        }).toList();

        // Auto mark as delivered - CHỈ cho status SENT
        if (!undeliveredIds.isEmpty()) {
            messageStatusRepo.markAsDelivered(currentUserId, undeliveredIds, LocalDateTime.now());
        }

        return result;
    }

    private ConversationResponse mapToResponse(ConversationProjection proj) {
        // 1. Xử lý URL ảnh
        // SP đã trả về filename đúng (dù là avatar user hay ảnh nhóm) vào cột getGroupImageURL
        String finalAvatarUrl = null;
        if (proj.getGroupImageURL() != null) {
            // Convert filename -> Presigned URL (link xem được)
            finalAvatarUrl = minioService.getFileUrl(proj.getGroupImageURL());
        }

        // 2. Map dữ liệu vào DTO
        return ConversationResponse.builder()
                .conversationId(proj.getConversationID())
                // SQL đã tự chọn tên nhóm hoặc tên người chat
                .conversationName(proj.getConversationName())
                .avatarUrl(finalAvatarUrl)
                .isGroup(proj.getIsGroupChat())
                .unreadCount(proj.getUnreadCount())

                // 3. Thông tin tin nhắn cuối (Last Message)
                // SQL đã xử lý [Hình ảnh]/[Video] nếu content null
                .lastMessageContent(proj.getLastMessageContent())
                .lastMessageTime(proj.getLastMessageSentAt() != null ? proj.getLastMessageSentAt().toString() : null)

                // Nếu bạn muốn hiển thị thêm: Ai là người nhắn cuối?
                // (Bạn cần thêm field lastMessageSender vào DTO ConversationResponse trước)
                // .lastMessageSender(proj.getLastMessageSender()
                .build();
    }
    private String determineMediaType(String contentType) {
        if (contentType == null) return "FILE";
        if (contentType.startsWith("image")) return "IMAGE";
        if (contentType.startsWith("video")) return "VIDEO";
        if (contentType.startsWith("audio")) return "AUDIO";
        return "FILE";
    }
    // Thêm vào cuối class ChatServiceImpl

    private MessageStatusSummary buildStatusSummary(List<MessageStatus> statuses, int currentUserId) {
        List<UserReadStatus> readByUsers = statuses.stream()
                .filter(s -> s.getUser().getId() != currentUserId)
                .filter(s -> MessageStatus.MessageStatusEnum.READ.equals(s.getStatus()))
                .map(s -> UserReadStatus.builder()
                        .userId(s.getUser().getId())
                        .username(s.getUser().getUsername())
                        .status(MessageStatus.MessageStatusEnum.READ)
                        .readAt(s.getReadAt())
                        .build())
                .toList();

        int deliveredCount = (int) statuses.stream()
                .filter(s -> s.getUser().getId() != currentUserId)
                .filter(s -> MessageStatus.MessageStatusEnum.DELIVERED.equals(s.getStatus())
                        || MessageStatus.MessageStatusEnum.READ.equals(s.getStatus()))
                .count();

        return MessageStatusSummary.builder()
                .sentCount(1)
                .deliveredCount(deliveredCount)
                .readCount(readByUsers.size())
                .readByUsers(readByUsers)
                .build();
    }
}