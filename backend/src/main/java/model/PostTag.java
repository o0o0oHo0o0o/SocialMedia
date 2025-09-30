package model;

import Keys.PostTagId;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "PostTags")
public class PostTag {
    @EmbeddedId
    private PostTagId postTagsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "PostID", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taggedUserId")
    @JoinColumn(name = "TaggedUserID", nullable = false)
    private User taggedUser;

    @Column(name = "TaggedAt", nullable = false)
    private Date taggedDate;

}
