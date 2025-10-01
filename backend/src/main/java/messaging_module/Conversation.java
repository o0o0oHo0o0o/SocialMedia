package messaging_module;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name =  "Conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int conversationId;

    @Column(name = "ConversationName", nullable = false)
    private String conversationName;

    @Column(name = "IsGroupChat", nullable = false)
    private boolean isGroupChat;

    @Column(name = "CreatedAt", nullable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> messages;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ConversationMember> members;
}
