package model;

import Keys.FollowId;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Follows", schema = "CoreData")
public class Follow {
    @EmbeddedId
    private FollowId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("FollowId")
    @JoinColumn(name = "FollowerID", nullable = false)
    private User userFollower;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("FollowId")
    @JoinColumn(name = "FollowingID", nullable = false)
    private User userFollowing;

    @Column(name = "FollowedAt",  nullable = false)
    private Date followedDate;
}
