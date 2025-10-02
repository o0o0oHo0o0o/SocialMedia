package repository;

import coredata_module.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameAndPassword(String username, String password);

    Void deleteByUsername(String username);
    Void deleteByEmail(String email);
    Void deleteById(int id);
}
