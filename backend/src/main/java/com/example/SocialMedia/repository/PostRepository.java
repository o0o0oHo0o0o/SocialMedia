package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.Post;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @NonNull
    List<Post> findAll();
}
