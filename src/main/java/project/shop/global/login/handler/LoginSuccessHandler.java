package project.shop.global.login.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.shop.domain.member.repository.MemberRepository;
import project.shop.global.jwt.service.JwtService;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final MemberRepository memberRepository;
	
	@Value("${jwt.access.expiration}")
	private String accessTokenExpiration;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) throws IOException, ServletException {
		
		String email = extractUsername(authentication); // 인증 정보에서 Username(email) 추출
		String accessToken = jwtService.createAccessToken(email); // JwtService의 create 메서드를 사용해 AccessToken 발급
		String refreshToken = jwtService.createRefreshToken(); // JwtService의 create 메서드를 사용해 RefreshToken 발급
		
		jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken); // 응답 헤더에 상태와 토큰들 실어서 보냄
		
		memberRepository.findByEmail(email)
			.ifPresent(member -> {
				member.updateRefreshToken(refreshToken);
				memberRepository.saveAndFlush(member);
			});
		log.info("로그인에 성공하였습니다. 이메일: {}", email);
		log.info("AccessToken을 발급합니다. AccessToken: {}", accessToken);
		log.info("AccessToken 만료 기간: {}", accessTokenExpiration);
	}
	
	
	
	private String extractUsername(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		
		return userDetails.getUsername();
	}
}