package com.example.SocialMedia.service;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.PostMedia;
import com.example.SocialMedia.repository.PostMediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public interface PostMediaService{
    String createPostMedia(MultipartFile file, Post post) throws IOException;
    String deletePostMedia(PostMedia postMedia);
}
