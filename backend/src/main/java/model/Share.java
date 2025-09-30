package model;

import Keys.ShareId;
import jakarta.persistence.*;
import org.hibernate.mapping.Join;

import java.util.Date;

@Entity
@Table(name = "Shares")
public class Share {
    @EmbeddedId
    private ShareId shareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "PostID", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "SharedAt", nullable = false)
    private Date sharedDate;

}
