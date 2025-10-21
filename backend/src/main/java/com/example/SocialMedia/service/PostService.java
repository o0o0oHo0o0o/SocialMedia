package com.example.SocialMedia.service;

import com.example.SocialMedia.model.coredata_model.Post;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface PostService {
    Post getPostById(long id);
}
