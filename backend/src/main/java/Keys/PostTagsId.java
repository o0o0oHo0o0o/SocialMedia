package Keys;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PostTagsId implements Serializable {
    private int postId;
    private int taggedUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostTagsId postTagId = (PostTagsId) o;
        if (postId != postTagId.postId) return false;
        return taggedUserId == postTagId.taggedUserId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, taggedUserId);
    }
}
