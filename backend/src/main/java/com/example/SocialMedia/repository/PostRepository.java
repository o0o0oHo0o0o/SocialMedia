package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Override
    List<Post> findAll();
}
