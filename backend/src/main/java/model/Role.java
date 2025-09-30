package model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private int RoleID;

    @Column(name = "RoleName", nullable = false)
    private String RoleName;

    @OneToMany(mappedBy = "role", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserRole> UserRoles;
}
