package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.request.ShareRequest;
import com.example.SocialMedia.dto.response.ShareResponse;
import com.example.SocialMedia.service.social.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping
        public ResponseEntity<ShareResponse> createShare(@RequestBody ShareRequest request,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
            ShareResponse response = shareService.createShare(request, userDetails.getUsername());
            return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShareResponse> getShareById(@PathVariable Integer id) {
        ShareResponse response = shareService.getShareById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
        public ResponseEntity<Page<ShareResponse>> getAllShares(Pageable pageable) {
            return ResponseEntity.ok(shareService.getAllShares(pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ShareResponse>> getSharesByUser(
            @PathVariable Integer userId,
                Pageable pageable) {
            return ResponseEntity.ok(shareService.getSharesByUserId(userId, pageable));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<ShareResponse>> getSharesByPost(
            @PathVariable Integer postId,
                Pageable pageable) {
            return ResponseEntity.ok(shareService.getSharesByPostId(postId, pageable));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShare(
            @PathVariable Integer id,
                @AuthenticationPrincipal UserDetails userDetails) {
            shareService.deleteShare(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
