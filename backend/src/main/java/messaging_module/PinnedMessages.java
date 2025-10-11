package messaging_module;

import keys.PinnedMessagesID;
import coredata_module.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "PinnedMessages")
@Getter
@Setter
public class PinnedMessages {
    @EmbeddedId
    private PinnedMessagesID pinnedMessagesID;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("messageID")
    @JoinColumn(name = "MessageID")
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationID")
    @JoinColumn(name = "ConversationID")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PinnedByUserID")
    private User user;

    @Column(name = "PinnedAt")
    private LocalDateTime pinnedAt;
}
