package coredata_module;

import jakarta.persistence.*;

@Entity
@Table(name = "PostMedia")
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MediaID")
    private int postMediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostID")
    private Post post;

    @Column(name = "MediaURL", unique = true, nullable = false)
    private String mediaURL;

    @Column(name = "MediaType", nullable = false)
    private String mediaType;

    @Column(name = "SortOrder", unique = true, nullable = false)
    private int sortOrder;

    public int getPostMediaId() {
        return postMediaId;
    }

    public void setPostMediaId(int postMediaId) {
        this.postMediaId = postMediaId;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
