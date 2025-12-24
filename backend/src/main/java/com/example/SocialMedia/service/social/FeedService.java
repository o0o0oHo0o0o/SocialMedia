package com.example.SocialMedia.service.social;

import com.example.SocialMedia.dto.response.PostResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

import com.example.SocialMedia.dto.response.FeedItemResponse;

public interface FeedService {
    List<PostResponse> getHome(String username, Pageable pageable);
    List<FeedItemResponse> getPopular(Pageable pageable);
    List<PostResponse> getDiscussion(Pageable pageable);
}