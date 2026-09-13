package com.scoring.pmescoring.config.security;

import com.auth0.jwt.JWT; // Import correto da biblioteca Auth0
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.scoring.pmescoring.common.exception.AuthExceptions;
import com.scoring.pmescoring.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.token.secret}")
    private String key;
    @Value("${api.identify.origin}")
    private String sourceID;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(key);
            return JWT.create()
                    .withIssuer(sourceID)
                    .withSubject(user.getEmail())
                    .withExpiresAt(this.expire())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new AuthExceptions("Error while generating token: " + exception.getMessage());
        }
    }

    private Instant expire() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    public String getSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(key);
            return JWT.require(algorithm)
                    .withIssuer(sourceID)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new AuthExceptions("Token invalid or expired: " + exception.getMessage());        }
    }
}