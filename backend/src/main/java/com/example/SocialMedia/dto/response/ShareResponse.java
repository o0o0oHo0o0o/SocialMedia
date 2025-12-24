package com.example.SocialMedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareResponse {
    private Integer shareId;
    private Integer originalPostId;
    private String originalPostContent;
    private ShortUserResponse user;
    private ShortUserResponse originalPostAuthor;
    private String shareCaption;
    private LocalDateTime createdAt;
}
