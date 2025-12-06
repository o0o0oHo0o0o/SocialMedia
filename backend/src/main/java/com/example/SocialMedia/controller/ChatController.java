package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.message.ConversationMemberDTO;
import com.example.SocialMedia.dto.message.WebSocketTokenResponse;
import com.example.SocialMedia.dto.request.MarkReadRequest;
import com.example.SocialMedia.dto.request.SendMessageRequest;
import com.example.SocialMedia.dto.response.ConversationResponse;
import com.example.SocialMedia.dto.response.MessageResponse;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.message.ChatService;
import com.example.SocialMedia.service.message.ConversationService;
import com.example.SocialMedia.service.message.MessageStatusService;
import com.example.SocialMedia.service.message.WebSocketSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageStatusService messageStatusService;
    private final WebSocketSessionService webSocketSessionService;
    private final UserRepository userRepository;
    private final ConversationService conversationService;

    // 2. Controller endpoint - Thêm vào ConversationController hoặc MessagingController
    @PutMapping("/{conversationId}/nickname")
    public ResponseEntity<?> updateNickname(
            @PathVariable Integer conversationId,
            @RequestBody ConversationMemberDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Integer userId = Integer.parseInt(userDetails.getUsername());
            conversationService.updateMemberNickname(conversationId, userId, request.getNickname());

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật biệt danh thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }
    // 1. Gửi tin nhắn (Text + File)
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestPart("data") String messageJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {

        System.out.println("[ChatController] sendMessage called");
        System.out.println("[ChatController] userDetails: " + (userDetails != null ? userDetails.getUsername() : "NULL"));
        System.out.println("[ChatController] messageJson: " + messageJson);
        System.out.println("[ChatController] files: " + (files != null ? files.size() : 0));
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile f = files.get(i);
                System.out.println("[ChatController]   [" + i + "] " + f.getOriginalFilename() + " - " + f.getSize() + " bytes");
            }
        }

        SendMessageRequest request = new ObjectMapper().readValue(messageJson, SendMessageRequest.class);
        System.out.println("[ChatController] Request parsed: " + request);

        assert userDetails != null;
        MessageResponse response = chatService.sendMessage(userDetails.getUsername(), request, files);
        return ResponseEntity.ok(response);
    }

    // 2. Đánh dấu đã đọc (Read Receipt)
    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @RequestBody MarkReadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        messageStatusService.markAsRead(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/ws-token")
    public ResponseEntity<WebSocketTokenResponse> issueToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();

        String username = auth.getName();
        var user = userRepository.findByUserNameWithRoles(username).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        var token = webSocketSessionService.generateToken(user);
        return ResponseEntity.ok(token);
    }
    // ChatController.java
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> listConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = auth.getName();
        var data = chatService.getUserConversations(username, page, size);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> listMessages(
            @PathVariable int conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int size) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = auth.getName();
        var data = chatService.getMessages(username, conversationId, page, size);
        return ResponseEntity.ok(data);
    }
}