package project.shop.global.login.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security 폼 기반의 UsernamePasswordAuthenticationFilter를 참고하여 만든 커스텀 필터
 * 구조가 거의 같고, Type이 Json인 Login만 처리하도록 설정하는 부분만 다름
 * Username : 회원 아이디 -> email로 설정
 * "/login" 요청이 왔을 때 Json 값을 매핑 처리하는 필터
 */
public class CustomJsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private static final String DEFAULT_LOGIN_REQUEST_URL = "/member/login"; // "/login"으로 오는 요청을 처리
	private static final String HTTP_METHOD = "POST"; // 로그인 HTTP 메서드는 POST
	private static final String CONTENT_TYPE = "application/json"; // Json 타입의 데이터로 오는 로그인 요청만 처리
	private static final String EMAIL_KEY = "email"; // 회원 로그인 시 이메일 요청 Json Key: "email"
	private static final String PASSWORD_KEY = "password"; // 회원 로그인 시 비밀번호 요청 Json Key: "password"
	private static final AntPathRequestMatcher DEFAULT_LOGIN_ANT_PATH_REQUEST_MATCHER =
			new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD); // "/login" + POST로 온 요청에 매칭
	
	private final ObjectMapper objectMapper;
	
	public CustomJsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
		super(DEFAULT_LOGIN_ANT_PATH_REQUEST_MATCHER); // 위에서 설정한 "/login" + POST로 온 요청을 처리하기위해 설정
		this.objectMapper = objectMapper;
	}

	/**
	 * [인증 처리 메서드]
	 * 
	 * UsernamePasswordAuthenticationFilter와 동일하게 UsernamePasswordAuthenticationToken 사용
	 * StreamUtils를 통해 request에서 messageBody(Json) 반환
	 * 요청 Json Example
	 * {
	 * 		"email": "aaa@bbb.com"
	 * 		"password": "test123@"
	 * }
	 * 꺼낸 messageBody를 objectMapper.readValue() 를 사용해 Map으로 변환 (Key: Json키 -> email, password)
	 * Map의 Key(email, password)로 해당 이메일, 비밀번호 추출 후
	 * UsernamePasswordAuthenticationToken의 파라미터 principal, credentials에 대입
	 * 
	 * AbstractAuthenticationProcessingFilter(부모)의 getAuthenticationManager()로 AuthenticationManager 객체를 반환 받은 후
     * authenticate()의 파라미터로 UsernamePasswordAuthenticationToken 객체를 넣고 인증 처리
     * (여기서 AuthenticationManager 객체는 ProviderManager -> SecurityConfig에서 설정)
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException {
		
		if (request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {
			throw new AuthenticationServiceException("Authentication Content-Type not Supported: " + request.getContentType());
		}
		
		String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
		
		Map<String, String> emailPasswordMap = objectMapper.readValue(messageBody, Map.class);
		
		String email = emailPasswordMap.get(EMAIL_KEY);
		String password = emailPasswordMap.get(PASSWORD_KEY);
		
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, password); // Principal과 credentials 전달
		
		return this.getAuthenticationManager().authenticate(authRequest);
	}
}