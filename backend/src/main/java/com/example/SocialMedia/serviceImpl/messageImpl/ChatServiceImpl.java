package com.example.SocialMedia.serviceImpl.messageImpl;

import com.example.SocialMedia.constant.ReactionType;
import com.example.SocialMedia.dto.message.MessageStatusSummary;
import com.example.SocialMedia.dto.message.UserReadStatus;
import com.example.SocialMedia.dto.projection.ConversationProjection;
import com.example.SocialMedia.dto.request.ReactionRequest;
import com.example.SocialMedia.dto.request.SendMessageRequest;
import com.example.SocialMedia.dto.response.*;
import com.example.SocialMedia.keys.MessageStatusID;
import com.example.SocialMedia.model.coredata_model.InteractableItems;
import com.example.SocialMedia.model.coredata_model.Reaction;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.model.messaging_model.*;
import com.example.SocialMedia.repository.InteractableItemRepository;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.repository.message.*;
import com.example.SocialMedia.service.IMinioService;
import com.example.SocialMedia.service.message.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.SocialMedia.model.messaging_model.MessageStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepo;
    private final MessageBodyRepository messageBodyRepo;
    private final MessageMediaRepository messageMediaRepo;
    private final MessageStatusRepository messageStatusRepo;
    private final ConversationRepository conversationRepo;
    private final ConversationMemberRepository conversationMemberRepo;
    private final InteractableItemRepository interactableItemRepo;
    private final ReactionRepository reactionRepo;
    private final UserRepository userRepo;
    private final IMinioService minioService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public MessageResponse sendMessage(String username, SendMessageRequest request, List<MultipartFile> files) {
        log.info("Sending message for user: {}", username);

        // 1. Fetch User & Conversation
        User sender = userRepo.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Conversation conversation = conversationRepo.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + request.getConversationId()));

        // ==================================================================
        // BƯỚC MỚI: Tạo InteractableItem trước
        // ==================================================================
        InteractableItems interactableItem = new InteractableItems();
        interactableItem.setItemType("MESSAGE"); // Hoặc dùng Enum nếu bạn có
        interactableItem.setCreatedAt(LocalDateTime.now());
        interactableItem = interactableItemRepo.save(interactableItem);

        // 2. Create Base Message
        Messages message = new Messages();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setSentAt(LocalDateTime.now());
        message.setMessageType("TEXT");

        // Gắn InteractableItem vừa tạo vào Message
        message.setInteractableItem(interactableItem);

        long nextSeq = conversation.getLastMessageID() + 1;
        message.setSequenceNumber(nextSeq);

        // Handle Reply
        if (request.getReplyToMessageId() != null) {
            messageRepo.findById(request.getReplyToMessageId()).ifPresent(message::setReplyMessage);
        }

        Messages savedMessage = messageRepo.save(message);

        // 3. Create Message Status for other members
        List<ConversationMember> members = conversationMemberRepo
                .findByConversation_ConversationId(conversation.getConversationId());
        int senderId = sender.getId();

        for (ConversationMember member : members) {
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

        // 4. Handle Message Body
        String bodyContent = request.getContent();

        // Nếu null hoặc rỗng thì gán chuỗi rỗng để tránh lỗi logic sau này
        if (bodyContent == null || bodyContent.trim().isEmpty()) {
            bodyContent = "";
        }

        try {
            messageBodyRepo.insertBody(savedMessage.getMessageId(), bodyContent);
        } catch (Exception e) {
            log.warn("Failed to insert message body for msgId: {}", savedMessage.getMessageId(), e);
        }

        // 5. Handle Media
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

        // 6. Update Conversation Metadata
        conversation.setLastMessageID((int) savedMessage.getMessageId());
        conversationRepo.save(conversation);

        // 7. Get Nickname
        ConversationMember memberInfo = conversationMemberRepo
                .findByConversation_ConversationIdAndUser_Id(conversation.getConversationId(), sender.getId())
                .orElse(null);
        String nickname = memberInfo != null ? memberInfo.getNickname() : null;

        // 8. Build Response
        MessageResponse response = MessageResponse.builder()
                .messageId(savedMessage.getMessageId())
                .conversationId(conversation.getConversationId())
                .content(request.getContent())
                .media(mediaDtos)
                .sender(SenderDto.fromUser(sender, nickname))
                .sentAt(savedMessage.getSentAt().toString())
                .replyToMessageId(request.getReplyToMessageId())
                .isRead(false)
                .isDelivered(false)
                .build();

        // 9. Push Real-time Event
        SocketResponse<MessageResponse> socketEvent = SocketResponse.<MessageResponse>builder()
                .type("NEW_MESSAGE")
                .payload(response)
                .build();

        messagingTemplate.convertAndSend("/topic/chat." + conversation.getConversationId(), socketEvent);

        return response;
    }

    // ... (Giữ nguyên các hàm getUserConversations và getMessages từ code đã fix trước đó)
    @Override
    public List<ConversationResponse> getUserConversations(String username, int page, int size) {
        User user = userRepo.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        int dbPage = (page > 0) ? page - 1 : 0;
        List<ConversationProjection> rawData = conversationRepo.getUserConversations(user.getId(), dbPage, size);
        return rawData.stream().map(this::mapToResponse).toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(String username, int conversationId, int page, int size) {
        User user = userRepo.findByUserName(username).orElseThrow();
        boolean isMember = conversationMemberRepo.existsByConversation_ConversationIdAndUser_Id(conversationId, user.getId());
        if (!isMember) throw new RuntimeException("Not a conversation member");

        int p = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(p, size);
        var pageMsgs = messageRepo.findByConversationIdOrderBySequenceNumberDesc(conversationId, pageable);
        List<Messages> messages = pageMsgs.getContent();
        if (messages.isEmpty()) return List.of();

        List<Long> ids = messages.stream().map(Messages::getMessageId).toList();
        int currentUserId = user.getId();

        // 1. Fetch dữ liệu phụ (Body, Media, Status)
        var bodiesByMsgId = messageBodyRepo.findByMessageIDIn(ids).stream().collect(Collectors.groupingBy(MessageBodies::getMessageID));
        var mediaByMsgId = messageMediaRepo.findByMessage_MessageIdIn(ids).stream().collect(Collectors.groupingBy(m -> m.getMessage().getMessageId()));
        var statusesByMsgId = messageStatusRepo.findByMessage_MessageIdIn(ids).stream().collect(Collectors.groupingBy(s -> s.getMessage().getMessageId()));

        // Lấy list InteractableItemID từ messages (lọc null để tránh lỗi)
        List<Integer> interactableItemIds = messages.stream()
                .map(m -> m.getInteractableItem() != null ? m.getInteractableItem().getInteractableItemId() : null)
                .filter(Objects::nonNull)
                .toList();

        // Query DB lấy reactions và gom nhóm theo ItemID
        var reactionsByItemId = reactionRepo.findByInteractableItems_InteractableItemIdIn(interactableItemIds)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getInteractableItems().getInteractableItemId()));
        // ------------------------------------------------

        List<Long> undeliveredIds = new ArrayList<>();
        List<MessageResponse> result = messages.stream().map(m -> {
            String content = bodiesByMsgId.getOrDefault(m.getMessageId(), List.of()).stream().findFirst().map(MessageBodies::getContent).orElse(null);
            List<MediaDto> mediaDtos = mediaByMsgId.getOrDefault(m.getMessageId(), List.of()).stream()
                    .map(media -> MediaDto.builder()
                            .url(minioService.getFileUrl(media.getMediaName()))
                            .type(media.getMediaType())
                            .fileName(media.getFileName())
                            .fileSize(media.getFileSize())
                            .build())
                    .toList();
            String nickname = conversationMemberRepo.findByConversation_ConversationIdAndUser_Id(m.getConversation().getConversationId(), m.getSender().getId()).map(ConversationMember::getNickname).orElse(null);
            boolean isMySent = m.getSender().getId() == currentUserId;
            List<MessageStatus> statuses = statusesByMsgId.getOrDefault(m.getMessageId(), List.of());
            MessageStatus relevantStatus = statuses.stream().filter(s -> isMySent == (s.getUser().getId() != currentUserId)).findFirst().orElse(null);
            boolean isRead = relevantStatus != null && MessageStatus.MessageStatusEnum.READ.equals(relevantStatus.getStatus());
            boolean isDelivered = relevantStatus != null && (MessageStatus.MessageStatusEnum.DELIVERED.equals(relevantStatus.getStatus()) || isRead);


            if (!isMySent && relevantStatus != null && MessageStatus.MessageStatusEnum.SENT.equals(relevantStatus.getStatus())) {
                undeliveredIds.add(m.getMessageId());
            }
            Integer itemId = m.getInteractableItem() != null ? m.getInteractableItem().getInteractableItemId() : null;
            List<Reaction> itemReactions = (itemId != null) ? reactionsByItemId.getOrDefault(itemId, List.of()) : List.of();

            // 3.1. Đếm số lượng (Dùng Enum làm Key)
            Map<ReactionType, Long> reactionCounts = itemReactions.stream()
                    .collect(Collectors.groupingBy(Reaction::getReactionType, Collectors.counting()));

            // 3.2. Tìm reaction của mình
            ReactionType myReactionType = itemReactions.stream()
                    .filter(r -> r.getUser().getId() == currentUserId)
                    .map(Reaction::getReactionType)
                    .findFirst()
                    .orElse(null);
            // ----------------------------------------
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
                    .reactionCounts(reactionCounts)
                    .myReactionType(myReactionType)
                    .build();
        }).toList();

        if (!undeliveredIds.isEmpty()) {
            messageStatusRepo.markAsDelivered(currentUserId, undeliveredIds, LocalDateTime.now());
        }
        return result;
    }

    @Override
    @Transactional
    public void reactToMessage(String username, ReactionRequest request) {
        // 1. Lấy User
        User user = userRepo.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Lấy Message để tìm InteractableItem
        Messages message = messageRepo.findById(request.getMessageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Lưu ý: Message của bạn liên kết với InteractableItems
        InteractableItems item = message.getInteractableItem();
        // (Hoặc message.getInteractableItemID() tùy vào mapping trong Entity Message của bạn)

        if (item == null) {
            throw new RuntimeException("Tin nhắn này không thể tương tác");
        }

        // 3. Xử lý Reaction (Thêm/Sửa/Xóa)
        Optional<Reaction> existingReactionOpt = reactionRepo
                .findByInteractableItemsAndUser_Id(item, user.getId());

        String action = ""; // Để log hoặc bắn socket

        if (existingReactionOpt.isPresent()) {
            Reaction existingReaction = existingReactionOpt.get();

            // SO SÁNH ENUM
            if (existingReaction.getReactionType() == request.getReactionType()) {
                reactionRepo.delete(existingReaction);
                action = "REMOVED";
            } else {
                // Update
                existingReaction.setReactionType(request.getReactionType());
                existingReaction.setReactedLocalDateTime(LocalDateTime.now());
                reactionRepo.save(existingReaction);
                action = "UPDATED";
            }
        } else {
            // Chưa có -> Tạo mới
            if (request.getReactionType() != null) {
                Reaction newReaction = new Reaction();
                newReaction.setInteractableItems(item);
                newReaction.setUser(user);
                newReaction.setReactionType(request.getReactionType());
                newReaction.setReactedLocalDateTime(LocalDateTime.now());
                reactionRepo.save(newReaction);
                action = "ADDED";
            }
        }

        // 4. Bắn Socket Real-time (Quan trọng)
        Map<String, Object> socketPayload = new HashMap<>(); // Dùng HashMap thay vì Map.of
        socketPayload.put("conversationId", message.getConversation().getConversationId());
        socketPayload.put("messageId", message.getMessageId());
        socketPayload.put("userId", user.getId());
        socketPayload.put("username", user.getUsername());
        socketPayload.put("action", action);
        socketPayload.put("reactionType", action.equals("REMOVED") ? null : request.getReactionType().name());

        SocketResponse<Object> socketEvent = SocketResponse.builder()
                .type("REACTION_UPDATE") // Frontend sẽ lắng nghe type này
                .payload(socketPayload)
                .build();

        messagingTemplate.convertAndSend("/topic/chat." + message.getConversation().getConversationId(), socketEvent);
    }

    // (Các hàm helper mapToResponse, determineMediaType, buildStatusSummary giữ nguyên như cũ)
    private ConversationResponse mapToResponse(ConversationProjection proj) {
        String finalAvatarUrl = null;
        if (proj.getGroupImageURL() != null) {
            finalAvatarUrl = minioService.getFileUrl(proj.getGroupImageURL());
        }

        return ConversationResponse.builder()
                .conversationId(proj.getConversationID())
                .conversationName(proj.getConversationName())
                .avatarUrl(finalAvatarUrl)
                .isGroup(proj.getIsGroupChat())
                .unreadCount(proj.getUnreadCount())
                .lastMessageContent(proj.getLastMessageContent())
                .lastMessageTime(proj.getLastMessageSentAt() != null ? proj.getLastMessageSentAt().toString() : null)
                .build();
    }

    private String determineMediaType(String contentType) {
        if (contentType == null) return "FILE";
        if (contentType.startsWith("image")) return "IMAGE";
        if (contentType.startsWith("video")) return "VIDEO";
        if (contentType.startsWith("audio")) return "AUDIO";
        return "FILE";
    }

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