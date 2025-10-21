package com.example.SocialMedia.keys;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ConversationMembersID implements Serializable {
    private int conversationID;
    private int userID;

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationMembersID that = (ConversationMembersID) o;
        return conversationID == that.conversationID && userID == that.userID;
    }
    @Override
    public int hashCode(){
        return Objects.hash(conversationID, userID);
    }
}
