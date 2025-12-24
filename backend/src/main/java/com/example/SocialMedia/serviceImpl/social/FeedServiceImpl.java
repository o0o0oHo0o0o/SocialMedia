package com.example.SocialMedia.serviceImpl.social;

import com.example.SocialMedia.dto.response.PostResponse;
import com.example.SocialMedia.exception.ResourceNotFound.UserNotFoundException;
import com.example.SocialMedia.mapper.PostMapper;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.repository.*;
import com.example.SocialMedia.service.social.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    private final PostMapper postMapper;
    private final com.example.SocialMedia.repository.ShareRepository shareRepository;
    private final com.example.SocialMedia.mapper.FeedMapper feedMapper;

    // --- MAIN METHODS ---

    @Override
    public List<PostResponse> getHome(String username, Pageable pageable) {
        // Lấy ID từ username
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Gọi Repository
        return feedRepository.findFeed(user.getId(), pageable)
                .getContent().stream()
                .map(postMapper::toPostResponse) // <--- Gọi Mapper cực gọn
                .collect(Collectors.toList());
    }

    @Override
    public List<com.example.SocialMedia.dto.response.FeedItemResponse> getPopular(Pageable pageable) {
        // Fetch a larger window from DB to allow merging posts and shares
        int fetchLimit = Math.max(50, (pageable.getPageNumber() + 1) * pageable.getPageSize() * 2);

        // Posts already ordered by engagement (and recent share) from repository
        var posts = feedRepository.findPopular(org.springframework.data.domain.PageRequest.of(0, fetchLimit)).getContent();

        // Shares ordered by latest shared time
        var shares = shareRepository.findAll(org.springframework.data.domain.PageRequest.of(0, fetchLimit, org.springframework.data.domain.Sort.by("sharedLocalDateTime").descending())).getContent();

        var postItems = posts.stream().map(feedMapper::fromPost).collect(Collectors.toList());
        var shareItems = shares.stream().map(feedMapper::fromShare).collect(Collectors.toList());

        // Merge and sort by score desc then sortTime desc
        var combined = new java.util.ArrayList<com.example.SocialMedia.dto.response.FeedItemResponse>();
        combined.addAll(postItems);
        combined.addAll(shareItems);

        combined.sort((a, b) -> {
            int cmp = Long.compare(b.getScore(), a.getScore());
            if (cmp != 0) return cmp;
            java.time.LocalDateTime ta = a.getSortTime();
            java.time.LocalDateTime tb = b.getSortTime();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), combined.size());
        if (start >= combined.size()) return java.util.Collections.emptyList();

        return combined.subList(start, end);
    }

    @Override
    public List<PostResponse> getDiscussion(Pageable pageable) {
        return feedRepository.findDiscussion(pageable)
                .getContent()
                .stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
    }
}