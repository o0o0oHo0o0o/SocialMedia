package com.example.SocialMedia.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortUserResponse {
    int id;
    String name;
    public ShortUserResponse(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
