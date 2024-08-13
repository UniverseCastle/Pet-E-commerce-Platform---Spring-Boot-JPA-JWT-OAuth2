package project.shop.global.jwt.service;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface JwtService {

	String createAccessToken(String email);
	
	String createRefreshToken();
	
	void updateRefreshToken(String email, String refreshToken);
	
	void destroyRefreshToken(String email); // 토큰 파기
	
	void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken);
	
	void sendAccessToken(HttpServletResponse response, String accessToken);
	
	Optional<String> extractAccessToken(HttpServletRequest request);
	
	Optional<String> extractRefreshToken(HttpServletRequest request);
	
	Optional<String> extractEmail(String accessToken);
	
	void setAccessTokenHeader(HttpServletResponse response, String accessToken);
	
	void setRefreshTokenHeader(HttpServletResponse response, String refreshToken);
	
	boolean isTokenValid(String token); // 유효성 검사
	
}