package com.example.SocialMedia.config;

import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Optional;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final String FRONTEND_URL = "http://localhost:3000";
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new ServletException("Email not provided by OAuth2 provider");
        }
        String name = oAuth2User.getAttribute("name");
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            User userDetails = user.get();
            String jwtToken = jwtService.generateToken(null, userDetails);

        }

    }
}
