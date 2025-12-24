package com.example.SocialMedia.service.social;

import com.example.SocialMedia.dto.request.ShareRequest;
import com.example.SocialMedia.dto.response.ShareResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ShareService {
    ShareResponse createShare(ShareRequest request, String username);
    ShareResponse getShareById(Integer shareId);
    Page<ShareResponse> getAllShares(Pageable pageable);
    Page<ShareResponse> getSharesByUserId(Integer userId, Pageable pageable);
    Page<ShareResponse> getSharesByPostId(Integer postId, Pageable pageable);
    ShareResponse updateShare(Integer shareId, ShareRequest request, String username);
    void deleteShare(Integer shareId, String username);
}
