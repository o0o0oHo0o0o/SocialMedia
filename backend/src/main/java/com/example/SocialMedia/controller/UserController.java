package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.response.ShortUserResponse;
import com.example.SocialMedia.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<ShortUserResponse>> getUserByKeyword(
            @PathVariable String keyword,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getUserByKeyword(keyword, pageable));
    }
}
