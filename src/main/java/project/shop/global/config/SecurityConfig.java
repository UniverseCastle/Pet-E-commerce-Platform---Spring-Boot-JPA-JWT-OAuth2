package project.shop.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import project.shop.domain.member.repository.MemberRepository;
import project.shop.global.jwt.filter.JwtAuthenticationProcessingFilter;
import project.shop.global.jwt.service.JwtService;
import project.shop.global.login.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import project.shop.global.login.handler.LoginFailureHandler;
import project.shop.global.login.handler.LoginSuccessHandler;
import project.shop.global.login.service.LoginService;
import project.shop.global.oauth2.handler.OAuth2LoginFailureHandler;
import project.shop.global.oauth2.handler.OAuth2LoginSuccessHandler;
import project.shop.global.oauth2.service.CustomOAuth2UserService;

/**
 * 인증은 CustomJsonUsernamePasswordAuthenticationFilter에서 authenticate()로 인증된 사용자로 처리
 * JwtAuthenticationProcessingFilter는 AccessToken, RefreshToken 재발급
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // Spring Security 설정 활성화
public class SecurityConfig {
	
	private final JwtService jwtService;
	private final LoginService loginService;
	private final MemberRepository memberRepository;
	private final ObjectMapper objectMapper;
	private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
	private final OAuth2LoginFailureHandler oauth2LoginFailureHandler;
	private final CustomOAuth2UserService customOAuth2UserService;
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.formLogin(formLogin -> formLogin.disable()) // FormLogin 사용 x
			.httpBasic(basic -> basic.disable()) // httpBasic 사용 x
			.csrf(csrf -> csrf.disable()) // csrf 보안 사용 x, REST API를 사용하여 인증정보를 저장하지 않고 JWT토큰, OAuth2를 담아서 요청하므로 disable
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 x
			.authorizeHttpRequests(auth -> auth // URL 별 관리 옵션
//					.requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
					.requestMatchers("/", "/main", "/index.html", "/member/oauth2/signUp", "/member/signUp", "/member/loginHub",
									 "/css/**", "/images/**", "/js/**", "/favicon.ico", "/member/guestSearchOrder",
									 "/member/login").permitAll()
					.anyRequest().authenticated()) // 위의 경로 이외에는 모두 인증된 사용자만 접근 가능
			
			//== 소셜 로그인 설정 ==//
			.oauth2Login(oauth2 -> oauth2
					.successHandler(oauth2LoginSuccessHandler) // 동의하고 계속하기를 눌렀을 때 Handler 설정
					.failureHandler(oauth2LoginFailureHandler) // 소셜 로그인 실패 시 핸들러 설정
					.userInfoEndpoint(user -> user.userService(customOAuth2UserService)) // customUserService 설정
			)
			
//			.formLogin(login -> login
//					.loginPage("/member/login")
//					.usernameParameter("email"))
			
			// 원래 스프링 시큐리티 필터 순서가 LogoutFilter 이후에 로그인 필터 동작
			// 따라서, LogoutFilter 이후에 커스텀한 필터가 동작되도록 설정
			// 순서 : LogoutFilter -> JwtAuthenticationProcessingFilter -> CustomJsonUsernamePasswordAuthenticationFilter
			.addFilterAfter(customJsonUsernamePasswordLoginFilter(), LogoutFilter.class)
			.addFilterBefore(jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordAuthenticationFilter.class)
			;
			
		
		return http.build();
		
		
	}
	

	
	// 비밀번호 암호화
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	
	
	/**
	 * AuthenticationManager 설정 후 등록
	 * PasswordEncoder를 사용하는 AuthenticationProvider 지정 (PasswordEncoder는 위에서 등록한 PasswordEncoder 사용)
	 * FormLogin(기존 스프링 시큐리티 로그인)과 동일하게 DaoAuthenticationProvider 사용
	 * UserDetailsService는 커스텀 LoginService로 등록
	 * 또한, FormLogin과 동일하게 AuthenticationManager로는 구현체인 ProviderManager 사용(return ProviderManager)
	 *
	 */
	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(loginService);
		
		return new ProviderManager(provider);
	}
	
	
	
	/**
	 * 로그인 성공 시 호출되는 LoginSuccessJWTProviderHandler 빈 등록
	 */
	@Bean
	public LoginSuccessHandler loginSuccessHandler() {
		return new LoginSuccessHandler(jwtService, memberRepository);
	}
	
	/**
	 * 로그인 실패 시 호출되는 LoginFailureHandler 빈 등록
	 */
	@Bean
	public LoginFailureHandler loginFailureHandler() {
		return new LoginFailureHandler();
	}
	
	
	
	/**
     * CustomJsonUsernamePasswordAuthenticationFilter 빈 등록
     * 커스텀 필터를 사용하기 위해 만든 커스텀 필터를 Bean으로 등록
     * setAuthenticationManager(authenticationManager())로 위에서 등록한 AuthenticationManager(ProviderManager) 설정
     * 로그인 성공 시 호출할 handler, 실패 시 호출할 handler로 위에서 등록한 handler 설정
     */
	@Bean
	public CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordLoginFilter() {
		CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordLoginFilter
			= new CustomJsonUsernamePasswordAuthenticationFilter(objectMapper);
		
		customJsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
		customJsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
		customJsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
		
		return customJsonUsernamePasswordLoginFilter;
	}
	
	
	
	@Bean
	public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
		JwtAuthenticationProcessingFilter jwtAuthenticationFilter = new JwtAuthenticationProcessingFilter(jwtService, memberRepository);
		
		return jwtAuthenticationFilter;
	}
}