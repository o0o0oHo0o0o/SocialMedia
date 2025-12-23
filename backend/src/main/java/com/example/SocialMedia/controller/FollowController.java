package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.response.ShortUserResponse;
import com.example.SocialMedia.service.social.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;
    @GetMapping("/{username}")
    public ResponseEntity<Boolean> getFollower(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        return ResponseEntity.ok(followService.getFollow(username, userDetails.getUsername()));
    }
    @PostMapping("/{username}")
    public ResponseEntity<ShortUserResponse> createFollow(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails // <--- Bảo mật
    ) {
        // Truyền username xuống service
        return ResponseEntity.ok(followService.createFollower(username, userDetails.getUsername()));
    }
}
