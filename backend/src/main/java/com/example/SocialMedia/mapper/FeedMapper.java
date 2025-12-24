package com.example.SocialMedia.mapper;

import com.example.SocialMedia.dto.response.FeedItemResponse;
import com.example.SocialMedia.dto.response.FeedItemType;
import com.example.SocialMedia.dto.response.PostResponse;
import com.example.SocialMedia.dto.response.ShareResponse;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.Share;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class FeedMapper {

    private final PostMapper postMapper;

    public FeedItemResponse fromPost(Post post) {
        PostResponse pr = postMapper.toPostResponse(post);
        long score = computeScore(pr);
        return FeedItemResponse.builder()
                .type(FeedItemType.POST)
                .post(pr)
                .score(score)
                .sortTime(pr.getCreatedAt())
                .build();
    }

    public FeedItemResponse fromShare(Share share) {
        // Reuse PostMapper to get original post info
        Post original = share.getPost();
        PostResponse originalPostResp = postMapper.toPostResponse(original);

        ShareResponse sr = ShareResponse.builder()
                .shareId(share.getShareId())
                .originalPostId(original.getPostId())
                .originalPostContent(original.getContent())
                .user(new com.example.SocialMedia.dto.response.ShortUserResponse(share.getUser().getId(), share.getUser().getUsername()))
                .originalPostAuthor(new com.example.SocialMedia.dto.response.ShortUserResponse(original.getUser().getId(), original.getUser().getUsername()))
                .shareCaption(share.getShareCaption())
                .createdAt(share.getSharedLocalDateTime())
                .build();

        long score = computeScore(originalPostResp);

        return FeedItemResponse.builder()
                .type(FeedItemType.SHARE)
                .post(originalPostResp)
                .share(sr)
                .score(score)
                .sortTime(share.getSharedLocalDateTime())
                .build();
    }

    private long computeScore(PostResponse pr) {
        AtomicLong total = new AtomicLong();
        total.addAndGet(pr.getCommentCount());
        total.addAndGet(pr.getShareCount());
        Map<String, Long> reactions = pr.getReactionCounts();
        if (reactions != null) reactions.values().forEach(total::addAndGet);
        return total.get();
    }
}