package com.example.SocialMedia.serviceImpl.messageImpl;

import com.example.SocialMedia.constant.MessageType;
import com.example.SocialMedia.constant.ReactionType;
import com.example.SocialMedia.constant.TargetType;
import com.example.SocialMedia.dto.message.MessageStatusSummary;
import com.example.SocialMedia.dto.message.ReplyMessageDto;
import com.example.SocialMedia.dto.message.UserReadStatus;
import com.example.SocialMedia.dto.projection.ConversationProjection;
import com.example.SocialMedia.dto.request.CreateConversationRequest;
import com.example.SocialMedia.dto.request.CreatePrivateChatRequest;
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
        message.setMessageType(MessageType.TEXT);

        // Gắn InteractableItem vừa tạo vào Message
        message.setInteractableItem(interactableItem);

        long nextSeq = conversation.getLastMessageID() + 1;
        message.setSequenceNumber(nextSeq);

        // Handle Reply
        if (request.getReplyToMessageId() != null) {
            // 1. Tìm tin nhắn gốc từ DB
            Messages originalMessage = messageRepo.findById(request.getReplyToMessageId())
                    .orElseThrow(() -> new RuntimeException("Tin nhắn phản hồi không tồn tại (ID: " + request.getReplyToMessageId() + ")"));

            // 2. Kiểm tra xem tin nhắn gốc có thuộc cùng cuộc trò chuyện không (Bảo mật)
            if (!(originalMessage.getConversation().getConversationId()==conversation.getConversationId())) {
                throw new RuntimeException("Không thể reply tin nhắn của cuộc trò chuyện khác");
            }

            // 3. SET vào Entity (Để Hibernate tự tạo khóa ngoại self-referencing)
            message.setReplyMessage(originalMessage);
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


        //reply message
        // ... (Code lưu DB ở trên giữ nguyên) ...

        // reply message DTO construction
        ReplyMessageDto replyDto = null;

        // Kiểm tra tin nhắn vừa lưu có quan hệ reply không
        if (savedMessage.getReplyMessage() != null) {
            Messages original = savedMessage.getReplyMessage(); // Tin nhắn gốc

            // 1. Lấy nội dung text (nếu có)
            String originalContent = (original.getMessageBody() != null)
                    ? original.getMessageBody().getContent()
                    : "";

            // 2. Kiểm tra Media và xác định loại
            boolean hasMedia = original.getMessageMedia() != null && !original.getMessageMedia().isEmpty();
            String detectedMediaType = null;

            if (hasMedia) {
                // Lấy file đầu tiên để xác định loại đại diện
                MessageMedia media = original.getMessageMedia().stream().findFirst().orElse(null);
                detectedMediaType = media.getMediaType();
            }

            replyDto = ReplyMessageDto.builder()
                    .messageId(original.getMessageId())
                    .content(originalContent)
                    .senderName(original.getSender().getFullName())
                    .hasMedia(hasMedia)
                    .mediaType(detectedMediaType) // <--- [MỚI] Gán loại media vào đây
                    .build();
        }
        // 8. Build Response
        MessageResponse response = MessageResponse.builder()
                .messageId(savedMessage.getMessageId())
                .conversationId(conversation.getConversationId())
                .content(request.getContent())
                .media(mediaDtos)
                .sender(SenderDto.fromUser(sender, nickname))
                .sentAt(savedMessage.getSentAt().toString())
                .replyToMessageId(request.getReplyToMessageId())
                .replyToMessage(replyDto)
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

    @Override
    public List<ConversationResponse> getUserConversations(String username, int page, int size) {
        User user = userRepo.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        //Đặt giá trị mặc định nếu size <= 0
        if (size <= 0) {
            size = 10;
        }
        int dbPage = (page > 0) ? page - 1 : 0;
        List<ConversationProjection> rawData = conversationRepo.getUserConversations(
                user.getId(),
                size,
                dbPage
        );

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
            ReplyMessageDto replyDto = null;
            if (m.getReplyMessage() != null) {
                Messages original = m.getReplyMessage();

                // 1. Lấy Content tin nhắn gốc
                String originalContent = "";
                if (original.getMessageBody() != null) {
                    originalContent = original.getMessageBody().getContent();
                }

                // 2. Check Media tin nhắn gốc
                boolean hasMedia = original.getMessageMedia() != null && !original.getMessageMedia().isEmpty();
                String detectedMediaType = null;
                if (hasMedia) {
                    // Lấy phần tử đầu tiên an toàn bằng Stream
                    var firstMedia = original.getMessageMedia().stream().findFirst().orElse(null);
                    detectedMediaType = firstMedia.getMediaType();
                }

                replyDto = ReplyMessageDto.builder()
                        .messageId(original.getMessageId())
                        .content(originalContent)
                        .senderName(original.getSender().getFullName())
                        .hasMedia(hasMedia)
                        .mediaType(detectedMediaType)
                        .build();
            }
            // ----------------------------------------
            return MessageResponse.builder()
                    .messageId(m.getMessageId())
                    .conversationId(m.getConversation().getConversationId())
                    .content(content)
                    .media(mediaDtos)
                    .sender(SenderDto.fromUser(m.getSender(), nickname))
                    .sentAt(m.getSentAt() != null ? m.getSentAt().toString() : null)
                    .replyToMessageId(m.getReplyMessage() != null ? m.getReplyMessage().getMessageId() : null)
                    .replyToMessage(replyDto)
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
        // 1. Validate User
        User user = userRepo.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 2. Lấy InteractableItem (Tách logic tìm kiếm ra hàm riêng)
        InteractableItems item = findInteractableItem(request);

        // 3. Xử lý Logic Reaction
        // Dùng Composite Key hoặc User + Item để tìm
        Optional<Reaction> existingReactionOpt = reactionRepo
                .findByInteractableItemsAndUser_Id(item, user.getId());

        String action;
        ReactionType newType = request.getReactionType();

        if (existingReactionOpt.isPresent()) {
            Reaction existing = existingReactionOpt.get();

            // CASE 1: Bấm trùng cái cũ -> XÓA (Toggle Off)
            if (existing.getReactionType() == newType) {
                reactionRepo.delete(existing);
                action = "REMOVED";
            }
            // CASE 2: Bấm cái khác -> CẬP NHẬT
            else {
                existing.setReactionType(newType);
                existing.setReactedLocalDateTime(LocalDateTime.now());
                reactionRepo.save(existing);
                action = "UPDATED";
            }
        } else {
            // CASE 3: Chưa có -> TẠO MỚI
            if (newType == null) {
                throw new IllegalArgumentException("Reaction type cannot be null for new reaction");
            }
            saveNewReaction(item, user, newType);
            action = "ADDED";
        }

        // 4. Bắn Socket (Nên gửi kèm danh sách count mới nhất)
        if (request.getTargetType() == TargetType.MESSAGE) {
            // Lấy lại Message để đảm bảo data
            messageRepo.findById((long) request.getTargetId()).ifPresent(message -> sendReactionSocket(message, user, action, newType));
        }

        return action;
    }

    @Override
    public ConversationResponse createConversation(String username, CreateConversationRequest request) {
        return null;
    }

    @Override
    @Transactional
    public ConversationResponse createPrivateConversation(String myUsername, CreatePrivateChatRequest request) {
        // 1. Lấy thông tin 2 người
        User me = userRepo.findByUserName(myUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User target = userRepo.findById(request.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // 2. Kiểm tra xem đã có đoạn chat 1-1 nào tồn tại chưa
        // Nếu có rồi -> Trả về luôn, KHÔNG TẠO MỚI
        Optional<Conversation> existing = conversationRepo.findExistingPrivateConversation(me.getId(), target.getId());
        if (existing.isPresent()) {
            return mapEntityToResponse(existing.get(), me.getId()); // Hàm map bạn đã có
        }

        // 3. Nếu chưa có -> Tạo Conversation mới
        Conversation conv = new Conversation();
        conv.setGroupChat(false);
        // Chat 1-1 thì ConversationName để null, Frontend tự hiển thị tên đối phương
        conv.setConversationName(null);
        conv.setLastMessageID(0);
        Conversation savedConv = conversationRepo.save(conv);

        // 4. Lưu 2 thành viên vào bảng ConversationMember
        // Role để là "MEMBER" hoặc null tùy DB của bạn cho phép
        saveMember(savedConv, me);
        saveMember(savedConv, target);

        return mapEntityToResponse(savedConv, me.getId());
    }
    // --- HÀM PHỤ TRỢ: CHUYỂN ĐỔI ENTITY -> RESPONSE ---
    private ConversationResponse mapEntityToResponse(Conversation conv, int currentUserId) {
        String name = conv.getConversationName();
        String avatar = null;

        // Lưu ý: Dùng Boolean.TRUE.equals để tránh NullPointerException nếu field là null
        if (Boolean.TRUE.equals(conv.isGroupChat())) {
            // TRƯỜNG HỢP GROUP
            if (conv.getGroupImageFile() != null) {
                avatar = minioService.getFileUrl(conv.getGroupImageFile());
            }
        } else {
            // TRƯỜNG HỢP 1-1: Phải tìm thông tin người kia
            Optional<ConversationMember> otherMemberOpt = conversationMemberRepo.findByConversation_ConversationId(conv.getConversationId())
                    .stream()
                    .filter(m -> m.getUser().getId() != currentUserId)
                    .findFirst();

            if (otherMemberOpt.isPresent()) {
                User other = otherMemberOpt.get().getUser();
                name = other.getFullName(); // Hoặc getNickname() nếu có logic nickname

                String rawAvatar = other.getProfilePictureURL();
                if (rawAvatar != null) {
                    if (rawAvatar.startsWith("http")) {
                        avatar = rawAvatar;
                    } else {
                        avatar = minioService.getFileUrl(rawAvatar);
                    }
                }
            }
        }

        return ConversationResponse.builder()
                .conversationId(conv.getConversationId())
                .conversationName(name)
                .avatarUrl(avatar)
                .isGroup(conv.isGroupChat())
                .unreadCount(0) // Mới tạo nên chưa có tin chưa đọc
                .lastMessageContent("Bắt đầu cuộc trò chuyện")
                // Nếu Entity không có field createdAt, bạn có thể dùng LocalDateTime.now() tạm
                .lastMessageTime(LocalDateTime.now().toString())
                .build();
    }
    // Hàm phụ lưu member cho gọn code
    private void saveMember(Conversation conv, User user) {
        ConversationMember member = new ConversationMember();
        member.setConversation(conv);
        member.setUser(user);
        member.setRole("MEMBER"); // Lưu cho có lệ, không quan trọng với chat 1-1
        member.setJoinedLocalDateTime(LocalDateTime.now());
        conversationMemberRepo.save(member);
    }

    // Hàm phụ trợ tách ra cho gọn
    private InteractableItems findInteractableItem(ReactionRequest request) {
        return switch (request.getTargetType()) {
            case MESSAGE -> messageRepo.findById((long) request.getTargetId())
                    .map(Messages::getInteractableItem)
                    .orElseThrow(() -> new RuntimeException("Message not found or has no interactable item"));
            case POST -> throw new UnsupportedOperationException("Post reaction not implemented yet");
            case COMMENT -> throw new UnsupportedOperationException("Comment reaction not implemented yet");
            default -> throw new IllegalArgumentException("Invalid Target Type");
        };
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
        String finalName = proj.getConversationName();

        if (Boolean.TRUE.equals(proj.getIsGroupChat())) {
            // TRƯỜNG HỢP GROUP CHAT
            if (proj.getGroupImageURL() != null) {
                finalAvatarUrl = minioService.getFileUrl(proj.getGroupImageURL());
            }
        } else {
            // TRƯỜNG HỢP CHAT 1-1
            // Lấy avatar của đối phương (đã thêm vào Projection ở bước 1)
            if (proj.getOtherUserAvatar() != null) {
                // Kiểm tra xem avatar user là link full (Google/Fb) hay tên file MinIO
                String avatarRaw = proj.getOtherUserAvatar();
                if (avatarRaw.startsWith("http")) {
                    finalAvatarUrl = avatarRaw;
                } else {
                    finalAvatarUrl = minioService.getFileUrl(avatarRaw);
                }
            }

            // Gán tên đối phương nếu tên conversation null
            if (proj.getOtherUserFullName() != null) {
                finalName = proj.getOtherUserFullName();
            }
        }

        return ConversationResponse.builder()
                .conversationId(proj.getConversationID())
                .conversationName(finalName)
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