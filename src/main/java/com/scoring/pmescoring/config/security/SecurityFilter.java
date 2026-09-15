package com.scoring.pmescoring.config.security;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final HandlerExceptionResolver resolver;

    public SecurityFilter(UserRepository userRepository,
                          TokenService tokenService,
                          @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = this.getToken(request);

            if (token != null) {
                String email = tokenService.getSubject(token);
                User user = userRepository.findByEmailAndStatus(email, EntityStatus.ACTIVE).orElse(null);;

                if (user != null) {
                    var userAuth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(userAuth);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            resolver.resolveException(request, response, null, ex);
        }
    }

    private String getToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }
        return null;
    }
}