package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FeedService {
    List<PostResponse> getHome(int userId, Pageable pageable);
    List<PostResponse> getPopular(Pageable pageable);
    List<PostResponse> getDiscussion(Pageable pageable);
}
