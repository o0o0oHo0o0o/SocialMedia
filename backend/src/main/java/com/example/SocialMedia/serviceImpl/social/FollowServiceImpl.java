package com.example.SocialMedia.serviceImpl.social;

import com.example.SocialMedia.dto.response.ShortUserResponse;
import com.example.SocialMedia.exception.ResourceNotFound.UserNotFoundException;
import com.example.SocialMedia.model.coredata_model.Follow;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.repository.FollowRepository;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.social.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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
        follow.setUserFollower(userFollower);
        follow.setUserFollowing(userFollowing);
        followRepository.save(follow);
        return new ShortUserResponse(
                userFollower.getId(),
                userFollower.getFullName(),
                userFollower.getUsername(),
                userFollower.getProfilePictureURL(),
                userFollower.getCreatedLocalDateTime());
    }
}
