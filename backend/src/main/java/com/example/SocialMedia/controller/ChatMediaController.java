package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.file.FileDto;
import com.example.SocialMedia.dto.file.PhotoDto;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.example.SocialMedia.dto.*;
import com.example.SocialMedia.service.message.ChatMediaService;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMediaController {

    private final ChatMediaService chatMediaService;

    @GetMapping("/{conversationId}/photos")
    public ResponseEntity<Page<PhotoDto>> getConversationPhotos(
            @PathVariable int conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Page<PhotoDto> photos = chatMediaService.getPhotos(conversationId, page, size);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/{conversationId}/files")
    public ResponseEntity<Page<FileDto>> getConversationFiles(
            @PathVariable int conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<FileDto> files = chatMediaService.getFiles(conversationId, page, size);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/{conversationId}/members/details")
    public ResponseEntity<List<MemberDto>> getConversationMembers(
            @PathVariable int conversationId
    ) {
        try {
            List<MemberDto> members = chatMediaService.getMembers(conversationId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}