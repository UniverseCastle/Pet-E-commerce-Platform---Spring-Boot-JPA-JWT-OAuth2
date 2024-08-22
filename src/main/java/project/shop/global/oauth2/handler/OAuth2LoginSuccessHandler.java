package project.shop.global.oauth2.handler;

import java.io.IOException;

import org.eclipse.jdt.internal.compiler.util.FloatUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.enums.Role;
import project.shop.domain.member.exception.MemberException;
import project.shop.domain.member.exception.MemberExceptionType;
import project.shop.domain.member.repository.MemberRepository;
import project.shop.global.jwt.service.JwtService;
import project.shop.global.jwt.service.JwtServiceImpl;
import project.shop.global.oauth2.CustomOAuth2User;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final JwtServiceImpl jwtServiceImpl;
	private final MemberRepository memberRepository;
	
	
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		log.info("OAuth2 Login 성공!");
		
		try {
			CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
			
			// Role이 GUEST일 경우는 처음 요청한 회원이므로 회원가입 페이지로 리다이렉트
			if (oauth2User.getRole() == Role.GUEST) {
				String accessToken = jwtService.createAccessToken(oauth2User.getEmail());
				response.addHeader(jwtServiceImpl.getAccessHeader(), "Bearer " + accessToken);
				response.sendRedirect("/member/oauth2/signUp"); // 프론트의 회원가입 추가정보 입력 폼으로 리다이렉트
				
				jwtService.sendAccessAndRefreshToken(response, accessToken, null);
				
//				Member findMember = memberRepository.findByEmail(oauth2User.getEmail()).orElseThrow(() ->
//						new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));
//				
//				findMember.addUserAuthority(); // USER 로 권한 업데이트
			}else {
				loginSuccess(response, oauth2User); // 로그인에 성공한 경우 access, refresh 토큰 생성
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	
	// TODO : 소셜 로그인 시에도 무조건 토큰 생성하지 말고 JWT 인증 필터처럼 RefreshToken 유/무에 따라 다르게 처리하기
	private void loginSuccess(HttpServletResponse response, CustomOAuth2User oauth2User) throws IOException {
		String accessToken = jwtService.createAccessToken(oauth2User.getEmail());
		String refreshToken = jwtService.createRefreshToken();
		
		response.addHeader(jwtServiceImpl.getAccessHeader(), "Bearer " + accessToken);
		response.addHeader(jwtServiceImpl.getRefreshHeader(), "Bearer " + refreshToken);
		
		jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
		jwtService.updateRefreshToken(oauth2User.getEmail(), refreshToken);
	}
	
}