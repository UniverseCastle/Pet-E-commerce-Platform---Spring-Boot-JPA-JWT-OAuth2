package project.shop.global.jwt.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.repository.MemberRepository;
import project.shop.global.jwt.service.JwtService;
import project.shop.global.util.PasswordUtil;

/**
 * Jwt 인증 필터
 * "/login" 이외의 URI 요청이 왔을 때 처리하는 필터
 * 
 * 기본적으로 사용자는 요청 헤더에 AccessToken만 담아서 요청
 * AccessToken 만료시에만 RefreshToken을 요청
 * 헤더에 AccessToken과 함께 요청
 * 
 * 1. RefreshToken이 없고, AccessToken이 유효한 경우 -> 인증 성공 처리, RefreshToken 재발급x
 * 2. RefreshToken이 없고, AccessToken이 없거나 유효하지 않은 경우 -> 인증 실패 처리, 403 Error
 * 3. RefreshToken이 있는 경우 -> DB의 RefreshToken과 비교하여 일치하면 AccessToken 재발급, RefrechToken 재발급 (RTR방식)
 * 							   인증 성공 처리는 하지않고 실패 처리
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
	private final MemberRepository memberRepository;
	
	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
	
	private static final String NO_CHECK_URL = "/login"; // 경로로 들어오는 요청에 대해서는 작동x

	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		/**
		 * "/login" 요청이 들어오면 filterChain.doFilter() 를 호출하여 다음 필터로 제어를 넘김
		 * 순서에 맞게 다음 필터(또는 서블릿)를 호출하여 넘어감
		 * 
		 * return; 을 통해 다음 필터를 호출한 다음, 현재 필터의 진행을 막음
		 * return; 을 하지 않으면 다음 필터를 호출한 후, 다음 필터로 바로 넘어가지 않고
		 * 아래 로직들을 다 수행한 후에 넘어가기 때문에 return으로 바로 튕기도록 해야함
		 */
		if (request.getRequestURL().equals(NO_CHECK_URL)) {
			filterChain.doFilter(request, response);
			
			return; // return으로 현재필터 진행 막기 (안해주면 아래로 내려가서 계속 필터를 진행함)
		}
		
		/**
		 * 사용자 요청 헤더에서 RefreshToken 추출
		 * -> RefreshToken이 없거나 유효하지 않다면 (DB에 저장된 RefreshToken과 다르다면) null 반환
		 * 사용자의 요청 헤더에 RefreshToken이 있는 경우, AccessToken이 만료되어 요청한 경우밖에 없음
		 * 따라서 위의 경우를 제외하면 추출한 RefreshToken은 모두 null
		 */
		String refreshToken = jwtService
				.extractRefreshToken(request)
				.filter(jwtService::isTokenValid) // 유효성 검사
				.orElse(null); // RefreshToken이 없거나 유효하지 않다면 null 발생
		
		/**
		 * RefreshToken이 요청 헤더에 존재하는 상황이라면
		 * 사용자가 AccessToken이 만료되어 RefreshToken까지 보낸 것이므로
		 * RefreshToken이 DB의 RefreshToken과 일치하는지 판단
		 * 일치한다면 AccessToken 재발급
		 * 
		 * checkRefreshTokenAndReIssueAccessToken() :
		 * AccessToken/RefreshToken을 재발급 해주는 메서드
		 */
		if (refreshToken != null) {
			checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
			
			return; // RefreshToken을 보낸 경우에는 AccessToken을 재발급하고 인증 처리는 하지 않도록 하기위해 바로 return으로 필터 진행 막음
		}
		
		/**
		 * RefreshToken이 없거나 유효하지 않다면, AccessToken을 검사하고 인증을 처리하는 로직 수행
		 * AccessToken이 없거나 유효하지 않다면, 인증 객체가 담기지 않은 상태로 다음 필터로 넘어가기 때문에 403 에러 발생
		 * AccessToken이 유효하다면, 인증 객체가 담긴 상태로 다음 필터로 넘어가기 때문에 인증 성공
		 */
		if (refreshToken == null) {
			checkAccessTokenAndAuthentication(request, response, filterChain);
		}
	}
	
	/**
	 * [RefreshToken으로 유저정보 찾기 & AccessToken/RefreshToken 재발급 메서드]
	 * 
	 * @param refreshToken 으로 DB에서 유저를 찾고, 해당 유저가 있다면
	 * JwtService.createAccessToken() 으로 AccessToken 생성
	 * 
	 * reIssueRefreshToken() 으로 RefreshToken 재발급 & DB에 RefreshToken 업데이트 메서드 호출
	 * 
	 * JwtService.sendAccessTokenAndRefreshToken() 으로 응답 헤더에 보냄
	 */
	private void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
		memberRepository.findByRefreshToken(refreshToken)
				.ifPresent(member -> {
					String reIssuedRefreshToken = reIssueRefreshToken(member);
					jwtService.sendAccessAndRefreshToken(
							response,
							jwtService.createAccessToken(member.getEmail()),
							reIssuedRefreshToken
					);
				});
	}
	
	/**
	 * [AccessToken 체크 & 인증 처리 메서드]
	 * 
	 * @param request에서 extractAccessToken() 으로 AccessToken 추출
	 * isTokenValid() 로 유효한 토큰인지 검증
	 * 유효한 토큰인 경우 AccessToken에서 extractEmail로 Email을 추출
	 * findByEmail() 로 해당 이메일을 사용하는 유저 객체 반환
	 * 
	 * 유저 객체를 saveAuthentication() 으로 인증 처리하여
	 * 인증 허가 처리된 객체를 SecurityContextHolder에 담음
	 * 다음 인증 필터 진행
	 */
	private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		log.info("checkAccessTokenAndAuthentication() 호출");
		
		jwtService.extractAccessToken(request)
				.filter(jwtService::isTokenValid)
				.ifPresent(accessToken -> jwtService.extractEmail(accessToken)
						.ifPresent(email -> memberRepository.findByEmail(email)
								.ifPresent(this::saveAuthentication))); // 인증정보 저장
				
		filterChain.doFilter(request, response);
	}
	
	/**
	 * [인증 허가 메서드]
	 * 
	 * @param member : 직접 만든 회원의 객체
	 * builder의 user : UserDetails의 User 객체
	 * 
	 * new UsernamePasswordAuthenticationToken() 으로 인증 객체인 Authentication 객체 생성
	 * 
	 * UsernamePasswordAuthenticationToken의 파라미터
	 * 1. 위에서 만든 UserDetailsUser 객체 (유저 정보)
	 * 2. credential (보통 비밀번호로, 인증시에는 null로 제거)
	 * 3. Collection < ? extends GrantedAuthority>로,
	 * UserDetails의 User 객체 안에 Set<GrantedAuthority> authorities가 있어서 Getter로 호출
	 * new NullAuthoritiesMapper() 로 GrantedAuthoritiesMapper 객체를 생성하고 mapAuthorities() 에 담음
	 * 
	 * SecurityContextHolder.createEmptyContext() 로 빈 SecurityContext 객체를 생성
	 * setAuthentication() 을 이용하여 위에서 만든 Authentication 객체에 대한 인증허가 처리
	 * 
	 * 소셜로그인의 경우 password = null 
	 * 인증 처리시 password가 null이면 안되므로 랜덤 패스워드를 임의로 부여해줌
	 */
	private void saveAuthentication(Member member) {
		String password = member.getPassword();
		
		if (password == null) { // 소셜로그인 유저의 비밀번호를 임의로 설정하여 소셜로그인 유저도 인증되도록 설정
			password = PasswordUtil.generateRandomPassword();
		}
		
		UserDetails user = User.builder()
				.username(member.getEmail())
				.password(member.getPassword())
				.roles(member.getRole().name())
				.build();
		
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				user, null, authoritiesMapper.mapAuthorities(user.getAuthorities()));
		
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}
	
	/**
	 * [RefreshToken 재발급 & DB에 RefreshToken 업데이트 메서드]
	 * 
	 * jwtService.createRefreshToken() 으로 RefreshToken 재발급
	 * DB에 재발급한 RefreshToken 업데이트 후 Flush
	 */
	private String reIssueRefreshToken(Member member) {
		String reIssuedRefreshToken = jwtService.createRefreshToken();
		member.updateRefreshToken(reIssuedRefreshToken);
		memberRepository.saveAndFlush(member);
		
		return reIssuedRefreshToken;
	}
	
}