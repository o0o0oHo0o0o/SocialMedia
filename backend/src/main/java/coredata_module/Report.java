package coredata_module;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Reports")
@Setter
@Getter
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReportID", nullable = false)
    private User reportUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReportedPostID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReportedCommentID")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "ReportedUserID")
    private User reportedUser;

    @Column(name = "Reason", nullable = false)
    private String reason;

    @Column(name = "ReportStatus")
    private String reportStatus;

    @Column(name = "ReportedAt", nullable = false)
    private LocalDateTime reportedLocalDateTime;
}
