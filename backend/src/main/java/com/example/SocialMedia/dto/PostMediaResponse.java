package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMediaResponse {
    private int id;
    private String mediaURL;
    private String mediaType;
    private int sortOrder;
}
