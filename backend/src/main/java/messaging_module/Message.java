package messaging_module;

import coredata_module.User;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Messages")
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

    @Column(name = "Content", nullable = false)
    private String content;

    @Column(name = "SentAt", nullable = false)
    private Date sentDate;
}
