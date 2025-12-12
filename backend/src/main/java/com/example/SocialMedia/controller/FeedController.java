package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.service.FeedService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class FeedController {
    private final FeedService feedService;
    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }
    @GetMapping("/{userId}")
    public List<PostResponse> getHome(@PathVariable int userId, Pageable pageable) {
        return feedService.getHome(userId, pageable);
    }
    @GetMapping("/popular")
    public List<PostResponse> getPopular(Pageable pageable) {
        return feedService.getPopular(pageable);
    }
    @GetMapping("/discussion")
    public List<PostResponse> getDiscussion(Pageable pageable) {
        return feedService.getDiscussion(pageable);
    }
}
