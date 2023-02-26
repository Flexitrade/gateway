package de.flexitrade.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.flexitrade.common.exception.ErrorException;
import de.flexitrade.common.persistence.repository.UserRepository;
import de.flexitrade.common.web.exception.ApiException;
import de.flexitrade.common.web.request.LoginRequest;
import de.flexitrade.common.web.request.TokenRefreshRequest;
import de.flexitrade.common.web.response.RefreshTokenResponse;
import de.flexitrade.common.web.response.TokenResponse;
import de.flexitrade.gateway.util.CryptoUtil;
import de.flexitrade.gateway.util.JwtConstants;
import de.flexitrade.gateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthService {
	private final UserRepository userRepository;
	private final JwtUtils jwtUtils;
	private final PasswordEncoder encoder;

	public ResponseEntity<?> createToken(LoginRequest loginRequest) {
		final var user = userRepository.findByUsername(loginRequest.getUsername());
		if (user.isEmpty()) {
			return new ApiException(HttpStatus.FORBIDDEN, "UNKNOWN_USER").toResponseEntity();
		}
		if (!user.get().getIsAccountNonLocked()) {
			return new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED").toResponseEntity();
		}
		if (!user.get().getIsAccountNonExpired()) {
			return new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_EXPIRED").toResponseEntity();
		}
		if (!user.get().getIsEnabled()) {
			return new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED").toResponseEntity();
		}
		if (encoder.matches(loginRequest.getPassword(), user.get().getPassword()) == false) {
			return new ApiException(HttpStatus.FORBIDDEN, "WRONG_PASSWORD").toResponseEntity();
		}

		String accessToken = jwtUtils.generate(user.get(), JwtUtils.TokenType.ACCESS);
		String refreshToken = jwtUtils.generate(user.get(), JwtUtils.TokenType.REFRESH);
		return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken, user.get().getId(), user.get().getUsername(),
				user.get().getEmail()));
	}

	public ResponseEntity<?> refreshToken(@Valid TokenRefreshRequest request) {
		String currentRefreshToken = request.getRefreshToken();
		try {
			jwtUtils.isValidRefreshToken(currentRefreshToken);

			final Claims claims = jwtUtils.getAllClaimsFromToken(currentRefreshToken);
			final String username = claims.get(JwtConstants.JWT_USERNAME, String.class);
			final var user = userRepository.findByUsername(username);
			if (user.isEmpty()) {
				return new ApiException(HttpStatus.FORBIDDEN, "UNKNOWN_USER").toResponseEntity();
			}

			final String accessToken = jwtUtils.generate(user.get(), JwtUtils.TokenType.ACCESS);
			final String refreshToken = jwtUtils.generate(user.get(), JwtUtils.TokenType.REFRESH);

			return ResponseEntity.ok(new RefreshTokenResponse(accessToken, refreshToken));
		} catch (ErrorException e) {
			return new ApiException(HttpStatus.BAD_REQUEST, e).toResponseEntity();
		}
	}

	public ResponseEntity<Object> encrypt(String plainText) {
		try {
            return ResponseEntity.ok(CryptoUtil.encrypt(plainText));
        } catch (Exception e) {
        	return new ApiException(HttpStatus.BAD_REQUEST, e).toResponseEntity();
        }
	}

	public ResponseEntity<Object> decrypt(String hash) {
        try {
            return ResponseEntity.ok(CryptoUtil.decrypt(hash));
        } catch (Exception e) {
        	return new ApiException(HttpStatus.BAD_REQUEST, e).toResponseEntity();
        }
    }
}
