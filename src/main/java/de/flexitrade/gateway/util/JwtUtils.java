package de.flexitrade.gateway.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.flexitrade.common.exception.ErrorException;
import de.flexitrade.common.persistence.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtils {

    public enum TokenType {
        ACCESS, REFRESH;
    }

    @Value("${server.security.authentication.jwt.token-validity-in-seconds}")
    private Long expirationAccess;

    @Value("${server.security.authentication.jwt.token-validity-in-seconds-for-refresh-token}")
    private Long expirationRefresh;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
    }

    @Autowired
	public JwtUtils(@Value("${server.security.authentication.jwt.token-validity-in-seconds}") Long expirationAccess,
			@Value("${server.security.authentication.jwt.token-validity-in-seconds-for-refresh-token}") Long expirationRefresh) {
		this.expirationAccess = expirationAccess;
		this.expirationRefresh = expirationRefresh;
	}

    public void isValidAccessToken(String token) throws ErrorException {
        isValidToken(token, TokenType.ACCESS);
    }

    public void isValidRefreshToken(String token) throws ErrorException {
        isValidToken(token, TokenType.REFRESH);
    }

    private void isValidToken(String token, TokenType typeToken) throws ErrorException {
        final Claims claims = getAllClaimsFromToken(token);
        final String type = claims.get(JwtConstants.JWT_TOKEN_TYPE, String.class);
        if (type.isBlank() || !typeToken.name().equals(type)) {
            throw new ErrorException(log, "JWT Token is of type " + (TokenType.ACCESS.equals(typeToken) ? TokenType.REFRESH.name() : TokenType.ACCESS.name()));
        }
    }

    public Claims getAllClaimsFromToken(String token) throws ErrorException {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        } catch (SignatureException e) {
            throw new ErrorException(log, "Invalid JWT signature", e);
        } catch (MalformedJwtException e) {
            throw new ErrorException(log, "Invalid JWT token", e);
        } catch (ExpiredJwtException e) {
            throw new ErrorException(log, "JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            throw new ErrorException(log, "JWT token is unsupported", e);
        } catch (IllegalArgumentException e) {
            throw new ErrorException(log, "JWT claims string is empty", e);
        } catch (Exception e) {
            throw new ErrorException(log, "JWT error", e);
        }
    }

    public String generate(User user, TokenType typeToken) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.JWT_USER_ID, user.getId());
        claims.put(JwtConstants.JWT_USERNAME, user.getUsername());
        claims.put(JwtConstants.JWT_PROFILE_ID, "-1");
        claims.put(JwtConstants.JWT_TOKEN_TYPE, typeToken);
        return doGenerateToken(claims, user.getUsername(), typeToken);
    }

    private String doGenerateToken(Map<String, Object> claims, String username, TokenType typeToken) {
        final long expirationSeconds = TokenType.ACCESS.equals(typeToken) ? expirationAccess : expirationRefresh;
        final long expirationTimeLong = expirationSeconds * 1000;

        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

}