package de.flexitrade.gateway.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtils {

    public static final String JWT_USER_ID = "jwt_user_id";
    public static final String JWT_USERNAME = "jwt_username";
    public static final String JWT_PROFILE_ID = "jwt_profile_id";
    public static final String JWT_TOKEN_TYPE = "jwt_token_type";
	
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

    public void isValidAccessToken(String token) throws Exception {
        isValidToken(token, TokenType.ACCESS);
    }

    public void isValidRefreshToken(String token) throws Exception {
        isValidToken(token, TokenType.REFRESH);
    }

    private void isValidToken(String token, TokenType typeToken) throws Exception {
        final Claims claims = getAllClaimsFromToken(token);
        final String type = claims.get(JWT_TOKEN_TYPE, String.class);
        if (type.isBlank() || !typeToken.name().equals(type)) {
            throw new Exception("JWT Token is of type " + (TokenType.ACCESS.equals(typeToken) ? TokenType.REFRESH.name() : TokenType.ACCESS.name()));
        }
    }

    public Claims getAllClaimsFromToken(String token) throws Exception {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    public String generate(String username, String user_id, String profile_id, TokenType typeToken) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put(JWT_USERNAME, username);
        claims.put(JWT_USER_ID, user_id);
        claims.put(JWT_PROFILE_ID, profile_id);
        return doGenerateToken(claims, username, typeToken);
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