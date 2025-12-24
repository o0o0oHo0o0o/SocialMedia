package com.example.SocialMedia.serviceImpl.social;

import com.example.SocialMedia.dto.request.ShareRequest;
import com.example.SocialMedia.dto.response.ShareResponse;
import com.example.SocialMedia.dto.response.ShortUserResponse;
import com.example.SocialMedia.exception.ResourceNotFound.ResourceNotFoundException;
import com.example.SocialMedia.model.coredata_model.InteractableItems;
import com.example.SocialMedia.constant.TargetType;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.Share;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.repository.InteractableItemRepository;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.repository.ShareRepository;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.social.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final ShareRepository shareRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final InteractableItemRepository interactableItemRepository;

    @Override
    @Transactional
    public ShareResponse createShare(ShareRequest request, String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = postRepository.findById(request.getOriginalPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Create InteractableItem for the share
        InteractableItems interactableItem = new InteractableItems();
        interactableItem.setItemType(TargetType.SHARE);
        interactableItem.setCreatedAt(LocalDateTime.now());
        interactableItem = interactableItemRepository.save(interactableItem);

        Share share = new Share();
        share.setUser(user);
        share.setPost(post);
        share.setInteractableItem(interactableItem);
        share.setShareCaption(request.getShareCaption());
        share.setSharedLocalDateTime(LocalDateTime.now());

        Share savedShare = shareRepository.save(share);

        return mapToResponse(savedShare);
    }

    @Override
    public ShareResponse getShareById(Integer shareId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found"));
        return mapToResponse(share);
    }

    @Override
    public Page<ShareResponse> getAllShares(Pageable pageable) {
        return shareRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ShareResponse> getSharesByUserId(Integer userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return shareRepository.findByUser(user, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<ShareResponse> getSharesByPostId(Integer postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return shareRepository.findByPost(post, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ShareResponse updateShare(Integer shareId, ShareRequest request, String username) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found"));

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (share.getUser().getId() != user.getId()) {
            throw new SecurityException("You can only update your own shares");
        }

        share.setShareCaption(request.getShareCaption());
        Share updatedShare = shareRepository.save(share);

        return mapToResponse(updatedShare);
    }

    @Override
    @Transactional
    public void deleteShare(Integer shareId, String username) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found"));

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (share.getUser().getId() != user.getId()) {
            throw new SecurityException("You can only delete your own shares");
        }

        shareRepository.delete(share);
    }

    private ShareResponse mapToResponse(Share share) {
        return ShareResponse.builder()
                .shareId(share.getShareId())
                .originalPostId(share.getPost().getPostId())
                .originalPostContent(share.getPost().getContent())
                .user(mapToShortUserResponse(share.getUser()))
                .originalPostAuthor(mapToShortUserResponse(share.getPost().getUser()))
                .shareCaption(share.getShareCaption())
                .createdAt(share.getSharedLocalDateTime())
                .build();
    }

    private ShortUserResponse mapToShortUserResponse(User user) {
        return new ShortUserResponse(
                user.getId(),
                user.getUsername()
        );
    }
}
