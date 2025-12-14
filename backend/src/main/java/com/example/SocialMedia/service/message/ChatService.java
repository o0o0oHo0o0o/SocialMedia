package com.example.SocialMedia.service.message;

import com.example.SocialMedia.dto.request.SendMessageRequest;
import com.example.SocialMedia.dto.response.ConversationResponse;
import com.example.SocialMedia.dto.response.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {
    MessageResponse sendMessage(String username, SendMessageRequest request, List<MultipartFile> files);
    List<ConversationResponse> getUserConversations(String username, int page, int size);
    List<MessageResponse> getMessages(String username, int conversationId, int page, int size);
}
