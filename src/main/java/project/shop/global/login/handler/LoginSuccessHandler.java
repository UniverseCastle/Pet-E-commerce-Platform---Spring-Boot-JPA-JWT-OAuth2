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
		
		String email = extractEmail(authentication); // 인증 정보에서 Username(email) 추출
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
	
	
	
	private String extractEmail(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		
		return userDetails.getUsername();
	}
	
	// TODO: 예외처리: DB에서 예외가 발생할 수 있으므로 처리하는 로직 구현
	// TODO: 토큰 서명 및 암호화: JWT를 생성할 때, 비밀 키를 사용하여 서명하고, 가능한 경우 Token을 암호화하여 보안 강화
	// TODO: access, refresh토큰의 사용: 클라이언트에서 Access 토큰이 만료되었을 때 RefreshToken을 사용하여 새로운 AccessToken 발급받는 로직 구현
}