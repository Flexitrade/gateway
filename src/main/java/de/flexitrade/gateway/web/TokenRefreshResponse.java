package de.flexitrade.gateway.web;

public record TokenRefreshResponse (String accessToken, String refreshToken, String tokenType) {
	public TokenRefreshResponse(String accessToken, String refreshToken) {
		this(refreshToken, refreshToken, "Bearer");
	}
}