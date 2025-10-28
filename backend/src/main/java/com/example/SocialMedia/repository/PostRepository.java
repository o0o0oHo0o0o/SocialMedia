package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    // Find a post by its ID
    Optional<Post> findByPostId(long postId);
    
    // Find all posts by a specific user
    List<Post> findByUser(User user);
}
