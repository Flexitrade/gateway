package de.flexitrade.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.flexitrade.common.web.request.LoginRequest;
import de.flexitrade.common.web.request.TokenRefreshRequest;
import de.flexitrade.gateway.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthController {
	private final AuthService authService;

	@PostMapping(value = "token")
	public ResponseEntity<?> token(@Valid @RequestBody LoginRequest loginRequest) {
		return authService.createToken(loginRequest);
	}

	@PostMapping(value = "refresh")
	public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
		return authService.refreshToken(request);
	}

	@GetMapping(value = "encrypt")
    public ResponseEntity<Object> encrypt(@RequestParam("plainText") String plainText) {
        return authService.encrypt(plainText);
    }

    @GetMapping(value = "decrypt")
    public ResponseEntity<Object> decrypt(@RequestParam("hash") String hash) {
        return authService.decrypt(hash);
    }
}