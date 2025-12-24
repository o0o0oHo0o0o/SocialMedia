package com.example.SocialMedia.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareRequest {
    private Integer originalPostId;
    private String shareCaption;
}
