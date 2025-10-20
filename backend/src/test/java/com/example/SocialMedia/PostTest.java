package com.example.SocialMedia;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.service.serviceImpl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.junit.jupiter.api.extension.ExtendWith;        // For @ExtendWith annotation (JUnit 5 extension)
import org.mockito.InjectMocks;                         // For @InjectMocks annotation
import org.mockito.Mock;                                // For @Mock annotation
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void testGetPostById() {
        Post post = new Post();
        post.setPostId(1);
        post.setContent("Example content");

        when(postRepository.findByPostId(1)).thenReturn(Optional.of(post));

        Post result = postService.getPostById(1);

        assertEquals("Example content", result.getContent());
    }
}
