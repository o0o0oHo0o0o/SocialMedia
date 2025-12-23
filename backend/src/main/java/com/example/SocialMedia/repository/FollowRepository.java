package com.example.SocialMedia.repository;

import com.example.SocialMedia.keys.FollowId;
import com.example.SocialMedia.model.coredata_model.Follow;
import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    Optional<Follow> findByUserFollowerAndUserFollowing(User userFollower, User userFollowing);
}
