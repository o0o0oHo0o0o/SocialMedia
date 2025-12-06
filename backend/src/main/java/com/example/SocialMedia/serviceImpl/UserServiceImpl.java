package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.SocialMedia.constant.OtpChannel;
import com.example.SocialMedia.constant.UsernameConstants;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final SecureRandom random = new SecureRandom();


    @Override
    public String generateUniqueUsername(String identifier, OtpChannel channel) {
        String baseUsername = "Debater_";
        String adj = UsernameConstants.ADJECTIVES[random.nextInt(UsernameConstants.ADJECTIVES.length)];
        String noun = UsernameConstants.NOUNS[random.nextInt(UsernameConstants.NOUNS.length)];
        String funUsername = baseUsername + noun + adj;
        if (userRepository.findByUserName(funUsername).isEmpty()) {
            return funUsername;
        }
        String hash = Integer
                .toHexString(identifier.hashCode())
                .replace("-", "");
        if (hash.length() > 4) hash = hash.substring(0, 4);
        String fallback = funUsername + hash;
        int counter = 1;
        while (userRepository.findByUserName(fallback).isPresent()) {
            fallback = funUsername + hash + counter++;
        }
        return fallback;
    }


    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}