package com.example.SocialMedia.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.SocialMedia.service.JwtService;
import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if(!isValidAuthHeader(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }
        try{
            final String jwt = extractToken(authHeader);
            final String username = jwtService.extractUsername(jwt);
            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if(jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = createAuthenticationToken(
                        userDetails, request
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("Authentication successful: {}", username);
            } else {
                log.debug("Authentication failure: {}", username);
            }
        }catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            response.setHeader("X-Token-Expired", "true");
        } catch (SignatureException e) {
            log.error("JWT token signature exception: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT token malformed: {}", e.getMessage());
        } catch (Exception e){
            log.error("JWT exception: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private boolean isValidAuthHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }
    private String extractToken(String authHeader) {
        return authHeader.substring(7);
    }
    private UsernamePasswordAuthenticationToken createAuthenticationToken(
            UserDetails userDetails,
            HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetails(request));
        return authenticationToken;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") || path.startsWith("/api/auth/logout");
    }
}
