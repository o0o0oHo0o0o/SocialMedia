package com.example.SocialMedia.model.messaging_model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name =  "Conversations")
@Getter
@Setter
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConversationID")
    private int conversationId;

    @Column(name = "GroupImageURL")
    private String groupImageURL;

    @Column(name = "ConversationName", nullable = false)
    private String conversationName;

    @Column(name = "IsGroupChat", nullable = false)
    private boolean isGroupChat;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdLocalDateTime;

    @Column(name = "LastMessageID")
    private int lastMessageID;

    @Column(name = "CreatedByUserID")
    private int createdByUserID;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> messages;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ConversationMember> members;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PinnedMessages> pinnedMessages;

}
