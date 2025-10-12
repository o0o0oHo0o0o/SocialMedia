package model.messaging_model;

import model.coredata_model.InteractableItems;
import model.coredata_model.Reaction;
import model.coredata_model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Messages")
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageID")
    private int messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConversationID", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SenderID", nullable = false)
    private User sender;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InteractableItemID")
    private InteractableItems interactableItem;

    @Column(name = "ReplyToMessageID")
    private int replyToMessageID;

    @Column(name = "Content", nullable = false)
    private String content;

    @Column(name = "MessageType")
    private String messageType;

    @Column(name = "SentAt", nullable = false)
    private LocalDateTime sentLocalDateTime;

    @Column(name = "isDeleted")
    private boolean isDeleted;

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private Set<MessageMedia> messageMedia;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "InteractableItemID", referencedColumnName = "InteractableItemID")
    private List<Reaction> reactions;
}
