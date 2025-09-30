package model;

import Keys.ReactionId;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Reactions")
public class Reaction {
    @EmbeddedId
    private ReactionId reactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "PostID", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "ReactionType", nullable = false)
    private String reactionType;

    @Column(name = "ReactedAt", nullable = false)
    private Date reactedDate;


}
