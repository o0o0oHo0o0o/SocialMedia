package messaging_module;

import keys.ConversationMembersID;
import coredata_module.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Setter;
import lombok.Getter;

@Entity
@Table(name = "ConversationMembers")
@Getter
@Setter
public class ConversationMember {
    @EmbeddedId
    private ConversationMembersID conversationMembersID;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationID")
    @JoinColumn(name = "conversationID", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userID")
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Column(name = "Nickname")
    private String nickname;

    @Column(name = "LastReadMessageID")
    private String lastReadMessageID;

    @Column(name = "MutedUntil")
    private LocalDateTime  mutedUntil;

    @Column(name = "JoinedAt", nullable = false)
    private LocalDateTime joinedLocalDateTime;
}
