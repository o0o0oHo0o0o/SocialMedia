package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.repository.*;
import com.example.SocialMedia.service.FeedService;
import com.example.SocialMedia.service.PostMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedServiceImpl implements FeedService {
    private final FeedRepository feedRepository;
    private final ReactionRepository reactionRepository;
    private final ShareRepository shareRepository;
    private final CommentRepository commentRepository;
    private final PostMediaService postMediaService;
    int MAX_LOAD_MEDIA = 4;
    Pageable MEDIA_PAGEABLE = PageRequest.of(0, MAX_LOAD_MEDIA);

    @Autowired
    public FeedServiceImpl(FeedRepository feedRepository,
                           ReactionRepository reactionRepository,
                           ShareRepository shareRepository,
                           CommentRepository commentRepository,
                           PostMediaService postMediaService) {
        this.feedRepository = feedRepository;
        this.reactionRepository = reactionRepository;
        this.shareRepository = shareRepository;
        this.commentRepository = commentRepository;
        this.postMediaService = postMediaService;
    }
    private List<PostResponse> convertToPostResponse(Page<Post> posts){
        return posts.getContent().stream()
                .map(post -> new PostResponse.PostResponseBuilder()
                        .id(post.getPostId())
                        .content(post.getContent())
                        .postTopic(post.getPostTopic())
                        .location(post.getLocation())
                        .username(post.getUser().getUsername())
                        .createdAt(post.getCreatedLocalDateTime())
                        .interactableItemId(post.getInteractableItem().getInteractableItemId())
                        .updatedAt(post.getUpdatedLocalDateTime())
                        .commentCount(commentRepository.countCommentByPost(post))
                        .reactionCount(reactionRepository.countReactionsByInteractableItemId(post.getInteractableItem().getInteractableItemId()))
                        .shareCount(shareRepository.countShareByPost(post))
                        .medias(postMediaService.findByPost(post, MEDIA_PAGEABLE))
                        .build())
                .collect(Collectors.toList());
    }

    public List<PostResponse> getHome(int userId, Pageable pageable) {
        return convertToPostResponse(feedRepository.findFeed(userId, pageable));
    }
    public List<PostResponse> getPopular(Pageable pageable) {
        return convertToPostResponse(feedRepository.findPopular(pageable));
    }
    public List<PostResponse> getDiscussion(Pageable pageable) {
        return convertToPostResponse(feedRepository.findDiscussion(pageable));
    }
}
