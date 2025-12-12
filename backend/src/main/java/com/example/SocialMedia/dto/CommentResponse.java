package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CommentResponse {
    private int id;
    private String content;
    private ShortUserResponse user;
    private Integer interactableItemId;
    private Integer parentCommentId;
    private List<Object[]> reaction;
    private boolean replied;
    private LocalDateTime createdAt;
    private CommentResponse(PostMediaResponseBuilder builder){
        this.id = builder.id;
        this.content = builder.content;
        this.user = builder.user;
        this.interactableItemId = builder.interactableItemId;
        this.replied = builder.replied;
        this.reaction = builder.reaction;
        this.createdAt = builder.createdAt;
        this.parentCommentId = builder.parentCommentId;
    }
    public static class PostMediaResponseBuilder {
        private int id;
        private String content;
        private ShortUserResponse user;
        private Integer interactableItemId;
        private Integer parentCommentId;
        private boolean replied = false;
        private List<Object[]> reaction;
        private LocalDateTime createdAt;
        public PostMediaResponseBuilder id(int id) {
            this.id = id;
            return this;
        }
        public PostMediaResponseBuilder content(String content) {
            this.content = content;
            return this;
        }
        public PostMediaResponseBuilder user(ShortUserResponse user) {
            this.user = user;
            return this;
        }
        public PostMediaResponseBuilder interactableItemId(Integer interactableItemId) {
            this.interactableItemId = interactableItemId;
            return this;
        }
        public PostMediaResponseBuilder parentCommentId(Integer parentCommentId) {
            this.parentCommentId = parentCommentId;
            return this;
        }
        public PostMediaResponseBuilder replied(boolean replied) {
            this.replied = replied;
            return this;
        }
        public PostMediaResponseBuilder reaction(List<Object[]> reaction) {
            this.reaction = reaction;
            return this;
        }
        public PostMediaResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        public CommentResponse build() {
            return new CommentResponse(this);
        }
    }
}
