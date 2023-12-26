package de.flexitrade.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import de.flexitrade.gateway.util.JwtUtils;
import de.flexitrade.gateway.web.TokenRefreshResponse;
import de.flexitrade.gateway.web.TokenRefreshRequest;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatewayService {
	private final JwtUtils jwtUtils;

	public ResponseEntity<?> refreshToken(@Valid TokenRefreshRequest request) {
		String currentRefreshToken = request.getRefreshToken();
		try {
			jwtUtils.isValidRefreshToken(currentRefreshToken);
			final Claims claims = jwtUtils.getAllClaimsFromToken(currentRefreshToken);
			final String username = claims.get(JwtUtils.JWT_USERNAME, String.class);
			final String user_id  = claims.get(JwtUtils.JWT_USER_ID, String.class);
			final String profile_id  = claims.get(JwtUtils.JWT_PROFILE_ID, String.class);
			final String accessToken = jwtUtils.generate(username, user_id, profile_id, JwtUtils.TokenType.ACCESS);
			final String refreshToken = jwtUtils.generate(username, user_id, profile_id, JwtUtils.TokenType.REFRESH);
			return ResponseEntity.ok(new TokenRefreshResponse(accessToken, refreshToken));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e);
		}
	}
}
