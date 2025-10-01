package messaging_module;

import coredata_module.User;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "ConversationMembers")
public class ConversationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConversationMemberID", nullable = false)
    private int  conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConversationID", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "JoinedAt", nullable = false)
    private Date joinedDate;
}
