package com.example.SocialMedia.service.social;

import com.example.SocialMedia.dto.response.ShortUserResponse;

public interface FollowService {
    Boolean getFollow(String followerUsername, String followingUsername);
    ShortUserResponse createFollower(String followerUsername, String followingUsername);
    ShortUserResponse deleteFollower(String followerUsername, String followingUsername);
}
