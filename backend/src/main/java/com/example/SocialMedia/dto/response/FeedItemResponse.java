package com.example.SocialMedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedItemResponse {
    private FeedItemType type; // POST or SHARE

    // if type == POST -> post populated
    private PostResponse post;

    // if type == SHARE -> share populated and post contains original post
    private ShareResponse share;

    // meta for sorting/score
    private Long score;
    private LocalDateTime sortTime;
}