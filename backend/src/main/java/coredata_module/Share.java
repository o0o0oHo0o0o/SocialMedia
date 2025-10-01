package coredata_module;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Shares")
public class Share {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShareID")
    private int shareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostID", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "SharedAt", nullable = false)
    private Date sharedDate;

}
