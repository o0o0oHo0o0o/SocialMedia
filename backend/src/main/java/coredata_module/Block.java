package coredata_module;

import Keys.BlockId;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "Blocks")
public class Block {
    @EmbeddedId
    private BlockId blockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blockerId")
    @JoinColumn(name = "BlockerID", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blockedUserId")
    @JoinColumn(name = "BlockedUserID", nullable = false)
    private User blockedUser;

    @Column(name = "BlockedAt", nullable = false)
    private Date blockedDate;
}
