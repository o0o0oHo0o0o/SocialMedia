package model;

import Keys.PostTagsId;
import jakarta.persistence.*;
import org.hibernate.annotations.JoinFormula;

import java.util.Date;

@Entity
@Table(name = "PostTags")
public class PostTags {
    @EmbeddedId
    private PostTagsId postTagsId;

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
