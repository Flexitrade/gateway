package de.flexitrade.gateway.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.flexitrade.gateway.exception.ErrorException;
import de.flexitrade.gateway.util.JwtConstants;
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
public class JwtUtil {

    public enum TypeToken {
        ACCESS, REFRESH;
    }

    @Value("${jwt.expirationAccess}")
    private String expirationAccess;

    @Value("${jwt.expirationRefresh}")
    private String expirationRefresh;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
    }

    public void isValidAccessToken(String token) throws ErrorException {
        isValidToken(token, TypeToken.ACCESS);
    }

    public void isValidRefreshToken(String token) throws ErrorException {
        isValidToken(token, TypeToken.REFRESH);
    }

    private void isValidToken(String token, TypeToken typeToken) throws ErrorException {
        final Claims claims = getAllClaimsFromToken(token);
        final String type = claims.get(JwtConstants.JWT_TYPE_TOKEN, String.class);
        if (StringUtils.isBlank(type) || !typeToken.name().equals(type)) {
            throw new ErrorException(log, "JWT Token is of type " + (TypeToken.ACCESS.equals(typeToken) ? TypeToken.REFRESH.name() : TypeToken.ACCESS.name()));
        }
    }

    public Claims getAllClaimsFromToken(String token) throws ErrorException {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
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

    public String generate(UserDTO userDTO, TypeToken typeToken) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.JWT_USER_ID, userDTO.getId());
        claims.put(JwtConstants.JWT_USERNAME, userDTO.getUsername());
        claims.put(JwtConstants.JWT_PROFILE_ID, userDTO.getPerfil().getId());
        claims.put(JwtConstants.JWT_TYPE_TOKEN, typeToken);
        return doGenerateToken(claims, userDTO.getUsername(), typeToken);
    }

    private String doGenerateToken(Map<String, Object> claims, String username, TypeToken typeToken) {
        final long expirationSeconds = Long.parseLong(TypeToken.ACCESS.equals(typeToken) ? expirationAccess : expirationRefresh);
        final long expirationTimeLong = expirationSeconds * 1000;

        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }

}
