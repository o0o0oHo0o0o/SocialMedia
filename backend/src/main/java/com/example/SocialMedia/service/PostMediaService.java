package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.PostMediaResponse;
import com.example.SocialMedia.model.coredata_model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface PostMediaService{
    PostMediaResponse[] findByPost(Post post, Pageable pageable);
    void deleteByPost(Post post);
    void createPostMedia(MultipartFile file, Post post);
    void deletePostMedia(int id);
}
