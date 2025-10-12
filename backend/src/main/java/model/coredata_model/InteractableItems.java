package model.coredata_model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "InteractableItems")
@Setter
@Getter
public class InteractableItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InteractableItemID")
    private int interactableItemId;

    @Column(name = "ItemType")
    private String itemType;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;


}
