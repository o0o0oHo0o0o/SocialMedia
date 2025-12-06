package com.example.SocialMedia.serviceImpl.messageImpl;

import com.example.SocialMedia.dto.message.ConversationMemberDTO;
import com.example.SocialMedia.model.messaging_model.ConversationMember;
import com.example.SocialMedia.repository.message.ConversationMemberRepository;
import com.example.SocialMedia.service.message.ConversationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationMemberRepository conversationMemberRepo;

    @Override
    public ConversationMemberDTO updateMemberNickname(Integer conversationId, Integer userId, String nickname) {
        // Dùng phương thức đã tồn tại
        ConversationMember member = conversationMemberRepo
                .findByConversation_ConversationIdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("Conversation member not found"));

        // Cập nhật nickname
        member.setNickname(nickname);

        // Lưu và trả về DTO
        ConversationMember updated = conversationMemberRepo.save(member);
        return convertToDTO(updated);
    }
    private ConversationMemberDTO convertToDTO(ConversationMember member) {
        ConversationMemberDTO dto = new ConversationMemberDTO();
        dto.setUserId(member.getUser().getId());
        dto.setUsername(member.getUser().getUsername());
        dto.setProfilePictureUrl(member.getUser().getProfilePictureURL());
        dto.setNickname(member.getNickname());
        dto.setRole(member.getRole());
        return dto;
    }
}
