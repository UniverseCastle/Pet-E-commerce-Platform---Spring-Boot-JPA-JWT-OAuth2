package project.shop.global.jwt.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.enums.Role;
import project.shop.domain.member.repository.MemberRepository;
import project.shop.global.jwt.service.JwtService;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "dev-profile")
class JwtFilterAuthenticationTest {
	
	@Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Autowired
    JwtService jwtService;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();


	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;
	
	private static String KEY_EMAIL = "email";
	private static String KEY_PASSWORD = "password";
	private static String EMAIL = "spring@naver.com";
	private static String PASSWORD = "password1234@";
	
	private static String LOGIN_URL = "/login";
	
	
	private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
	private static final String BEARER = "Bearer ";
	
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	
	
	private void clear(){
		em.flush();
		em.clear();
	}


//	@BeforeEach
//	private void init(){
//		memberRepository.save(Member.builder()
//				.email(EMAIL)
//				.password(delegatingPasswordEncoder.encode(PASSWORD))
//				.name("Member1")
//				.nickName("NickName1")
//				.role(Role.USER)
//				.age(22)
//				.build());
//		clear();
//	}



	private Map getEmailPasswordMap(String email, String password){
		Map<String, String> map = new HashMap<>();
		map.put(KEY_EMAIL, email);
		map.put(KEY_PASSWORD, password);
		return map;
	}


	private Map getAccessAndRefreshToken() throws Exception {
	
		Map<String, String> map = getEmailPasswordMap(EMAIL, PASSWORD);
		
		System.out.println("========================================================Request Data: " + map);
		
		MvcResult result = mockMvc.perform(
						 post(LOGIN_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(map))
		).andExpect(status().isOk()).andReturn();
		
		String accessToken = result.getResponse().getHeader(accessHeader);
		String refreshToken = result.getResponse().getHeader(refreshHeader);
		
		Map<String, String> tokenMap = new HashMap<>();
		tokenMap.put(accessHeader,accessToken);
		tokenMap.put(refreshHeader,refreshToken);
		
		return tokenMap;
	}
	
	
	
	//== TEST ==//
	
	
	
	/**
	 * AccessToken : 존재하지 않음
	 * RefreshToken : 존재하지 않음
	 */
//	@Test
	public void 토큰_모두_존재x() throws Exception {
		// when, then
		mockMvc.perform(
				get(LOGIN_URL + "123")
		).andExpect(status().isForbidden());
	}
	
	
	
	/**
	 * AccessToken : 유효
	 * RefreshToken : 존재하지 않음
	 */
	@Test
	public void AccessToken만_보내서_인증() throws Exception {
        //given
        Map accessAndRefreshToken = getAccessAndRefreshToken();
        String accessToken= (String) accessAndRefreshToken.get(accessHeader);

        //when, then
        mockMvc.perform(get(LOGIN_URL+"123") //login이 아닌 다른 임의의 주소
        		.header(accessHeader,BEARER+ accessToken))
                .andExpectAll(status().isNotFound());//없는 주소로 보냈으므로 NotFound
    }
	
	
	
	/**
	 * AccessToken : 유효하지 않음,
	 * RefreshToken : 존재하지 않음
	 */
//	@Test
	public void 유효하지_않은_AccessToken만_보내서_인증x_상태코드는_403() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String accessToken = (String)accessAndRefreshToken.get(accessHeader);
		
		// when
		mockMvc.perform(
				get(LOGIN_URL + "123")
				.header(accessHeader, accessToken + "1") // login이 아닌 다른 임의의 주소
		)
				.andExpectAll(status().isForbidden()); // 없는 주소로 보냈으므로 NotFound
	}
	
	
	
	/**
	 * AccessToken : 존재하지 않음
	 * RefreshToken : 유효
	 */
//	@Test
	public void 유효한RefreshToken만_보내서_AccessToken_재발급_200() throws Exception {
		// given
		Map accessAndRefreshToken = getAccessAndRefreshToken();
		String refreshToken = (String)accessAndRefreshToken.get(refreshHeader);
		
		// when, then
		MvcResult result = mockMvc.perform(
				get(LOGIN_URL + "123").header(refreshHeader, BEARER + refreshToken) // login이 아닌 다른 임의의 주소
		)
				.andExpect(status().isOk()) // 응답 상태 200 OK 인지 검증
				.andReturn(); // 결과 반환
		
		String accessToken = result.getResponse().getHeader(accessHeader);
		
		String subject = JWT.require(Algorithm.HMAC512(secret)).build().verify(accessToken).getSubject();
		assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
	}
}