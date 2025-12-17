package com.example.SocialMedia.model.coredata_model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "PostMedia", schema = "CoreData")
@Setter
@Getter
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MediaID")
    private int postMediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostID")
    private Post post;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InteractableItemID")
    private InteractableItems interactableItem;

    @Column(name = "MediaURL", unique = true, nullable = false)
    private String mediaURL;

    @Column(name = "MediaType", nullable = false)
    private String mediaType;

    @Column(name = "SortOrder", unique = true, nullable = false)
    private int sortOrder;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "InteractableItemID",referencedColumnName = "InteractableItemID")
    private List<Reaction> reactions;

}
