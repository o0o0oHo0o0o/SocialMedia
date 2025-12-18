package com.example.SocialMedia.serviceImpl.messageImpl;

import com.example.SocialMedia.constant.ReactionType;
import com.example.SocialMedia.constant.TargetType;
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
import com.example.SocialMedia.repository.ReactionRepository;
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
        interactableItem.setItemType(TargetType.MESSAGE); // Hoặc dùng Enum nếu bạn có
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
    public String reactToMessage(String username, ReactionRequest request) {
        // 1. Lấy User
        User user = userRepo.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        InteractableItems item = null;
        Messages message = null;

        // 2. Xác định đối tượng (Dùng Enum TargetType)
        switch (request.getTargetType()) {
            case MESSAGE:
                message = messageRepo.findById((long) request.getTargetId())
                        .orElseThrow(() -> new RuntimeException("Message not found with id: " + request.getTargetId()));
                item = message.getInteractableItem();
                break;

            case POST:
                // TODO: Logic cho Post
                break;

            case COMMENT:
                // TODO: Logic cho Comment
                break;

            default:
                throw new IllegalArgumentException("Target type không hỗ trợ: " + request.getTargetType());
        }

        if (item == null) {
            throw new RuntimeException("Lỗi dữ liệu: Đối tượng này chưa có InteractableItem");
        }

        // 3. Xử lý Reaction dùng Optional
        Optional<Reaction> existingReactionOpt = reactionRepo
                .findByInteractableItemsAndUser_Id(item, user.getId());

        String action = "";

        if (existingReactionOpt.isPresent()) {
            Reaction existingReaction = existingReactionOpt.get();

            // Nếu bấm trùng loại cũ -> XÓA (Toggle Off)
            if (existingReaction.getReactionType() == request.getReactionType()) {
                reactionRepo.delete(existingReaction);
                action = "REMOVED";
            } else {
                // Nếu bấm loại khác -> CẬP NHẬT cái hiện tại
                existingReaction.setReactionType(request.getReactionType());
                existingReaction.setReactedLocalDateTime(LocalDateTime.now());
                reactionRepo.save(existingReaction);
                action = "UPDATED";
            }
        } else {
            // Chưa có -> TẠO MỚI (Dùng hàm helper)
            if (request.getReactionType() != null) {
                saveNewReaction(item, user, request.getReactionType());
                action = "ADDED";
            }
        }

        // 4. Bắn Socket (Chỉ bắn nếu là Message)
        if (request.getTargetType() == TargetType.MESSAGE) {
            sendReactionSocket(message, user, action, request.getReactionType());
        }
        return action;
    }

    // --- CÁC HÀM PHỤ TRỢ (HELPER METHODS) ---

    // Hàm lưu Reaction mới xuống DB
    private void saveNewReaction(InteractableItems item, User user, ReactionType type) {
        Reaction newReaction = new Reaction();
        newReaction.setInteractableItems(item);
        newReaction.setUser(user);
        newReaction.setReactionType(type);
        newReaction.setReactedLocalDateTime(LocalDateTime.now());
        reactionRepo.save(newReaction);
    }

    // Hàm bắn Socket
    private void sendReactionSocket(Messages message, User user, String action, ReactionType type) {
        Map<String, Object> socketPayload = new HashMap<>();
        socketPayload.put("conversationId", message.getConversation().getConversationId());
        socketPayload.put("messageId", message.getMessageId());

        // Thông tin người thả tim
        socketPayload.put("userId", user.getId());
        socketPayload.put("username", user.getUsername());
        socketPayload.put("fullName", user.getFullName());
        socketPayload.put("avatarUrl", user.getProfilePictureURL()); // Thêm avatar để UI cập nhật ngay

        // Thông tin hành động
        socketPayload.put("action", action); // ADDED, REMOVED, UPDATED
        socketPayload.put("reactionType", "REMOVED".equals(action) ? null : type);

        SocketResponse<Object> socketEvent = SocketResponse.builder()
                .type("REACTION_UPDATE")
                .payload(socketPayload)
                .build();

        messagingTemplate.convertAndSend("/topic/chat." + message.getConversation().getConversationId(), socketEvent);
    }

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