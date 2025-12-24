package com.example.SocialMedia.serviceImpl.social;

import com.example.SocialMedia.dto.response.ShortUserResponse;
import com.example.SocialMedia.exception.ResourceNotFound.ResourceNotFoundException;
import com.example.SocialMedia.exception.ResourceNotFound.UserNotFoundException;
import com.example.SocialMedia.keys.FollowId;
import com.example.SocialMedia.model.coredata_model.Follow;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.repository.FollowRepository;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.social.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    @Override
    public Boolean getFollow(String followerUsername, String followingUsername) {
        User userFollower = userRepository.findByUserName(followerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + followerUsername));
        User userFollowing = userRepository.findByUserName(followingUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + followingUsername));
        return followRepository.findByUserFollowerAndUserFollowing(userFollower, userFollowing).isPresent();
    }
    @Override
    public ShortUserResponse createFollower(String followerUsername, String followingUsername) {
        User userFollower = userRepository.findByUserName(followerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + followerUsername));
        User userFollowing = userRepository.findByUserName(followingUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + followingUsername));
        Follow follow = new Follow();
        FollowId followId = new FollowId();
        followId.setFollowerId(userFollower.getId());
        followId.setFollowingId(userFollowing.getId());

        follow.setFollowId(followId);  // Set the composite key
        follow.setUserFollower(userFollower);
        follow.setUserFollowing(userFollowing);
        follow.setFollowedLocalDateTime(LocalDateTime.now());
        followRepository.save(follow);
        return new ShortUserResponse(
                userFollower.getId(),
                userFollower.getFullName(),
                userFollower.getUsername(),
                userFollower.getProfilePictureURL(),
                userFollower.getCreatedLocalDateTime());
    }
    @Override
    public ShortUserResponse deleteFollower(String followerUsername, String followingUsername) {
        User userFollower = userRepository.findByUserName(followerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + followerUsername));
        User userFollowing = userRepository.findByUserName(followingUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + followingUsername));
        Follow follow = followRepository.findByUserFollowerAndUserFollowing(userFollower, userFollowing)
                .orElseThrow(() -> new ResourceNotFoundException("Follow not found: " + followerUsername));
        followRepository.delete(follow);
        return new ShortUserResponse(
                userFollower.getId(),
                userFollower.getFullName(),
                userFollower.getUsername(),
                userFollower.getProfilePictureURL(),
                userFollower.getCreatedLocalDateTime());
    }
}
