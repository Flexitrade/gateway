package de.flexitrade.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.flexitrade.gateway.service.GatewayService;
import de.flexitrade.gateway.web.TokenRefreshRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GatewayController {
	private final GatewayService gatewayService;

	@PostMapping(value = "refreshtoken")
	public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
		return gatewayService.refreshToken(request);
	}
}