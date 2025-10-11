package repository;

import keys.UserRoleId;
import coredata_module.Role;
import coredata_module.User;
import coredata_module.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    Optional<UserRole> findByRole(Role role);
    Optional<UserRole> findByUserAndRole(User user, Role role);
}
