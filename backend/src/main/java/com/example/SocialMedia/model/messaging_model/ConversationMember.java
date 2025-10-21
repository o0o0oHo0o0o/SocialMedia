package com.example.SocialMedia.model.messaging_model;

import com.example.SocialMedia.keys.ConversationMembersID;
import com.example.SocialMedia.model.coredata_model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Setter;
import lombok.Getter;

@Entity
@Table(name = "ConversationMembers", schema = "Messaging")
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
    private long lastReadMessageID;

    @Column(name = "MutedUntil")
    private LocalDateTime  mutedUntil;

    @Column(name = "JoinedAt", nullable = false)
    private LocalDateTime joinedLocalDateTime;
}
