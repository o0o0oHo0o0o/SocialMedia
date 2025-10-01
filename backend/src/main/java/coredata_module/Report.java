package coredata_module;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReportID", nullable = false)
    private User reportUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReportedPostID", nullable = true)
    private Post reportedPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReportedCommentID", nullable = true)
    private Comment reportedComment;

    @ManyToOne
    @JoinColumn(name = "ReportedUserID", nullable = true)
    private User reportedUser;

    @Column(name = "Reason", nullable = false)
    private String reason;

    @Column(name = "ReportStatus", nullable = true)
    private String reportStatus;

    @Column(name = "ReportedAt", nullable = false)
    private Date reportedDate;
}
