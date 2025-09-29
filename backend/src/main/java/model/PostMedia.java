package model;

import jakarta.persistence.*;

@Entity
@Table(name = "PostMedia")
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MediaID")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostID")
    private Post post;

    @Column(name = "MediaURL", unique = true, nullable = false)
    private String mediaURL;

    @Column(name = "MediaType", nullable = false)
    private String mediaType;

    @Column(name = "SortOrder", unique = true, nullable = false)
    private int sortOrder;



}
