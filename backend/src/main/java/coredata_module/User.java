package coredata_module;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import messaging_module.ConversationMember;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "Users")
@Setter
@Getter
public class User implements UserDetails {
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

    @Column(name = "ProfilePictureURL")
    private String profilePictureURL;

    @Column(name = "CreatedAt",  nullable = false)
    private LocalDateTime createdLocalDateTime;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Story> stories;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Share> shares;

    @OneToMany(mappedBy = "taggedUser", fetch = FetchType.LAZY)
    private List<PostTag> taggedPosts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ConversationMember> conversationMembers;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reaction> reactions;

    @OneToMany(mappedBy = "blocker", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Block> blocks;

    @OneToMany(mappedBy = "userFollower", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings;

    @OneToMany(mappedBy = "userFollowing", fetch = FetchType.LAZY)
    private List<Follow> followers;

    @OneToMany(mappedBy = "reportUser", fetch = FetchType.LAZY)
    private List<Report> reports;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream().map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getRoleName())).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
