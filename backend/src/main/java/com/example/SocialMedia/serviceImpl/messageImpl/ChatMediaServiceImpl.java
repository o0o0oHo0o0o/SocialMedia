package com.example.SocialMedia.serviceImpl.messageImpl;

import com.example.SocialMedia.dto.file.FileDto;
import com.example.SocialMedia.dto.file.PhotoDto;
import com.example.SocialMedia.repository.message.ConversationMemberRepository;
import com.example.SocialMedia.repository.message.ConversationRepository;
import com.example.SocialMedia.repository.message.MessageRepository;
import com.example.SocialMedia.service.message.ChatMediaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import com.example.SocialMedia.dto.*;
import com.example.SocialMedia.repository.*;
import com.example.SocialMedia.model.messaging_model.*;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.service.IMinioService;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMediaServiceImpl implements ChatMediaService {

    private final MessageRepository messageRepository;
    private final ConversationMemberRepository memberRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final IMinioService minioService;  // ✅ Thêm MinIO service

    public Page<PhotoDto> getPhotos(int conversationId, int page, int size) {
        verifyConversationAccess(conversationId);

        Page<Messages> photos = messageRepository.findPhotos(
                conversationId,
                PageRequest.of(page, size)
        );

        return photos.map(this::convertToPhotoDto);
    }

    public Page<FileDto> getFiles(int conversationId, int page, int size) {
        verifyConversationAccess(conversationId);

        Page<Messages> files = messageRepository.findFiles(
                conversationId,
                PageRequest.of(page, size)
        );

        return files.map(this::convertToFileDto);
    }

    public List<MemberDto> getMembers(int conversationId) {
        verifyConversationAccess(conversationId);

        List<ConversationMember> members = memberRepository.findByConversation_ConversationId(conversationId);

        return members.stream()
                .map(this::convertToMemberDto)
                .collect(Collectors.toList());
    }

    private PhotoDto convertToPhotoDto(Messages message) {
        List<String> urls = message.getMessageMedia().stream()
                .filter(mm -> "IMAGE".equals(mm.getMediaType()))
                .map(mm -> minioService.getFileUrl(mm.getFileName()))
                .collect(Collectors.toList());

        List<String> fileNames = message.getMessageMedia().stream()
                .filter(mm -> "IMAGE".equals(mm.getMediaType()))
                .map(MessageMedia::getFileName)
                .collect(Collectors.toList());

        return new PhotoDto(
                message.getMessageId(),
                urls,
                fileNames,
                "IMAGE",
                new UserSummary(
                        message.getSender().getId(),
                        message.getSender().getUsername(),
                        message.getSender().getFullName(),
                        message.getSender().getProfilePictureURL()
                ),
                message.getSentAt()
        );
    }

    // ✅ CHUYỂN ĐỔI MESSAGE -> FILEDTO (LẤY URL TỪ MINIO)
    private FileDto convertToFileDto(Messages message) {
        List<String> urls = message.getMessageMedia().stream()
                .filter(mm -> "FILE".equals(mm.getMediaType()))
                .map(mm -> minioService.getFileUrl(mm.getFileName()))  // ✅ Lấy URL từ MinIO
                .collect(Collectors.toList());

        List<String> fileNames = message.getMessageMedia().stream()
                .filter(mm -> "FILE".equals(mm.getMediaType()))
                .map(MessageMedia::getFileName)
                .collect(Collectors.toList());

        // ✅ FIX: Cast Integer sang Long
        List<Long> fileSizes = message.getMessageMedia().stream()
                .filter(mm -> "FILE".equals(mm.getMediaType()))
                .map(mm -> ((long) mm.getFileSize()))  // ✅ Cast Integer -> Long
                .collect(Collectors.toList());

        return new FileDto(
                message.getMessageId(),
                urls,
                fileNames,
                fileSizes,
                "FILE",
                new UserSummary(
                        message.getSender().getId(),
                        message.getSender().getUsername(),
                        message.getSender().getFullName(),
                        message.getSender().getProfilePictureURL()
                ),
                message.getSentAt()
        );
    }

    private MemberDto convertToMemberDto(ConversationMember member) {
        return new MemberDto(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getUser().getFullName(),
                member.getUser().getProfilePictureURL(),
                member.getNickname(),
                member.getJoinedLocalDateTime()
        );
    }

    private void verifyConversationAccess(int conversationId) {
        User currentUser = getCurrentUser();

        boolean isMember = conversationRepository.existsConversationMember(
                conversationId,
                currentUser.getId()
        );

        if (!isMember) {
            throw new RuntimeException("Bạn không phải thành viên của cuộc trò chuyện này");
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại"));
    }
}