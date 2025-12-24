package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.Share;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShareRepository extends JpaRepository<Share, Integer> {
    int countShareByPost(Post post);
    Page<Share> findByUser(User user, Pageable pageable);
    Page<Share> findByPost(Post post, Pageable pageable);
}
