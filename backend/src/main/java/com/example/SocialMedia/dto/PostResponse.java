package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PostResponse {

    private int id;
    private String content;
    private String postTopic;
    private String location;
    private String username; // from User
    private Integer interactableItemId;
    private List<Object[]> reactionCount;
    private int commentCount;
    private int shareCount;
    private PostMediaResponse[] medias;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    // Private constructor, to be used by the builder
    public PostResponse(PostResponseBuilder builder) {
        this.id = builder.id;
        this.content = builder.content != null ? builder.content : "";  // Default empty string if null
        this.postTopic = builder.postTopic != null ? builder.postTopic : "";  // Default empty string if null
        this.location = builder.location != null ? builder.location : "";  // Default empty string if null
        this.username = builder.username != null ? builder.username : "";  // Default empty string if null
        this.interactableItemId = builder.interactableItemId;
        this.reactionCount = builder.reactionCount;  // Default to 0
        this.commentCount = builder.commentCount != null ? builder.commentCount : 0;  // Default to 0
        this.shareCount = builder.shareCount != null ? builder.shareCount : 0;  // Default to 0
        this.medias = builder.medias != null ? builder.medias : new PostMediaResponse[0];  // Default to empty array
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : LocalDateTime.now();  // Default to now
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();  // Default to now
    }

    // Builder class
    public static class PostResponseBuilder {

        private Integer id;
        private String content;
        private String postTopic;
        private String location;
        private String username;
        private Integer interactableItemId;
        private List<Object[]> reactionCount;
        private Integer commentCount;
        private Integer shareCount;
        private PostMediaResponse[] medias;
        private LocalDateTime updatedAt;
        private LocalDateTime createdAt;

        // Builder methods for each field
        public PostResponseBuilder id(int id) {
            this.id = id;
            return this;
        }

        public PostResponseBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PostResponseBuilder postTopic(String postTopic) {
            this.postTopic = postTopic;
            return this;
        }

        public PostResponseBuilder location(String location) {
            this.location = location;
            return this;
        }

        public PostResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public PostResponseBuilder interactableItemId(Integer interactableItemId) {
            this.interactableItemId = interactableItemId;
            return this;
        }

        public PostResponseBuilder reactionCount(List<Object[]> reactionCount) {
            this.reactionCount = reactionCount;
            return this;
        }

        public PostResponseBuilder commentCount(int commentCount) {
            this.commentCount = commentCount;
            return this;
        }

        public PostResponseBuilder shareCount(int shareCount) {
            this.shareCount = shareCount;
            return this;
        }

        public PostResponseBuilder medias(PostMediaResponse[] medias) {
            this.medias = medias;
            return this;
        }

        public PostResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PostResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }


        // Build method to return the final PostResponse object
        public PostResponse build() {
            return new PostResponse(this);
        }
    }
}

