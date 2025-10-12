package repository;

import keys.UserRoleId;
import model.coredata_model.Role;
import model.coredata_model.User;
import model.coredata_model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    Optional<UserRole> findByRole(Role role);
    Optional<UserRole> findByUserAndRole(User user, Role role);
}
