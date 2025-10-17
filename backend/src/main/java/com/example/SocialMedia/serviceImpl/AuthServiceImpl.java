package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User register(User user) {
        return null;
    }
}
