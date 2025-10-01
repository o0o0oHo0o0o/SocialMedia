package coredata_module;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PostID", unique = true, nullable = false)
    private int postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User user;

    @Column(name = "Content")
    private String content;

    @Column(name = "PostType", nullable = false)
    private String postType;

    @Column(name = "Location", nullable = true)
    private String location;

    @Column(name = "IsArchived")
    private boolean isArchived = false;

    @Column(name = "CreatedAt", nullable = false)
    private Date createdDate;

    @Column(name = "UpdatedAt", nullable = true)
    private Date updatedDate;

    @OneToMany(mappedBy = "post", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<PostMedia> postMedias;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Share> shares;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reaction> reactions;
}
