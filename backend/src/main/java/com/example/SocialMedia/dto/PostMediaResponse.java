package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMediaResponse {
    private int id;
    private String mediaURL;
    private String mediaType;
    private int sortOrder;

    // Private constructor, to be used by the builder
    private PostMediaResponse(PostMediaResponseBuilder builder) {
        this.id = builder.id;
        this.mediaURL = builder.mediaURL != null ? builder.mediaURL : "";  // Default empty string if null
        this.mediaType = builder.mediaType != null ? builder.mediaType : "";  // Default empty string if null
        this.sortOrder = builder.sortOrder != null ? builder.sortOrder : 0;  // Default to 0
    }

    // Builder class
    public static class PostMediaResponseBuilder {
        private Integer id;
        private String mediaURL;
        private String mediaType;
        private Integer sortOrder;

        // Builder methods for each field
        public PostMediaResponseBuilder id(int id) {
            this.id = id;
            return this;
        }

        public PostMediaResponseBuilder mediaURL(String mediaURL) {
            this.mediaURL = mediaURL;
            return this;
        }

        public PostMediaResponseBuilder mediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public PostMediaResponseBuilder sortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        // Build method to return the final PostMediaResponse object
        public PostMediaResponse build() {
            return new PostMediaResponse(this);
        }
    }
}
