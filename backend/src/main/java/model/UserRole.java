package model;

import Keys.UserRoleId;
import jakarta.persistence.*;

@Entity
@Table(name = "UserRole")
public class UserRole {
    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "RoleID")
    private Role role;
}
