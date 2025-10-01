package coredata_module;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Reactions")
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReactionID")
    private int reactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostID", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "ReactionType", nullable = false)
    private String reactionType;

    @Column(name = "ReactedAt", nullable = false)
    private Date reactedDate;


}
