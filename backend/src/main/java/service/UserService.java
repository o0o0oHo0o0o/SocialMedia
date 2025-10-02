package service;

import coredata_module.Role;
import coredata_module.User;
import org.springframework.stereotype.Service;
import repository.RoleRepository;
import repository.UserRepository;
import repository.UserRoleRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }
    public User CreateUser(User user) {
        return userRepository.save(user);
    }
    public void DeleteUser(User user) {
        userRepository.delete(user);
    }
    public void DeleteUserById(int id) {
        userRepository.deleteById(id);
    }
    public void removeRoleFromUser(User user, Role role) {
        userRoleRepository.findByUserAndRole(user, role).ifPresent(userRole -> {
            user.getUserRoles().remove(userRole);
        });
    }
    public void removeRoleFromUserById(int userId, int roleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        userRoleRepository.findByUserAndRole(user, role).ifPresent(userRole -> {
            user.getUserRoles().remove(userRole);
        });
    }
}
