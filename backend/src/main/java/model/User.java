package model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private int id;

    @Column(name = "Username",  unique = true,  nullable = false)
    private String userName;

    @Column(name = "Email",   unique = true,  nullable = false)
    private String email;

    @Column(name = "PasswordHash",   unique = true,  nullable = false)
    private String password;

    @Column(name = "FullName", nullable = false)
    private String fullName;

    @Column(name = "Bio", nullable = false)
    private String bio;

    @Column(name = "ProfilePictureURL", nullable = true)
    private String profilePictureURL;

    @Column(name = "CreatedAt",  nullable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<Post> posts;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<UserRole> userRoles;

}
