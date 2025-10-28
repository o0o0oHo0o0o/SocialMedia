package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(int id);
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserNameAndPassword(String userName, String password);

    Void deleteByUserName(String userName);
    Void deleteByEmail(String email);
    Void deleteById(int id);
}
