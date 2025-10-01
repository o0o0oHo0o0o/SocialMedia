package Keys;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ShareId implements Serializable {
    private int  postId;
    private int userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShareId shareId = (ShareId) o;
        if (postId != shareId.postId) return false;
        return userId == shareId.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, userId);
    }
}
