package project.shop.global.jwt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.enums.Role;
import project.shop.domain.member.repository.MemberRepository;

@SpringBootTest
@Transactional
@ActiveProfiles(profiles = "dev-profile")
class JwtServiceTest {

	@Autowired
	JwtService jwtService;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	EntityManager em;
	
	@Value("${jwt.secret}")
	private String secret;
	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;
	
	private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer ";
	
    private String email = "spring@gmail.com";
    
//    @BeforeEach
//    public void init() {
//    	Member member = Member.builder()
//    			.email(email)
//    			.password("1234567890")
//    			.name("Member1")
//    			.nickName("NickName1")
//    			.role(Role.USER)
//    			.age(20)
//    			.build();
//    	memberRepository.save(member);
//    	clear();
//    }
    
    private void clear() {
    	em.flush();
    	em.clear();
    }
    
    private DecodedJWT getVerify(String token) {
    	return JWT.require(Algorithm.HMAC512(secret)).build().verify(token);
    }
    
    private HttpServletRequest setRequest(String accessToken, String refreshToken) throws IOException {

        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        // 응답 헤더에서 액세스 토큰과 리프레시 토큰을 추출
        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        // 추출한 토큰들을 헤더에 추가
        httpServletRequest.addHeader(accessHeader, BEARER+headerAccessToken);
        httpServletRequest.addHeader(refreshHeader, BEARER+headerRefreshToken);

        return httpServletRequest;
    }
	
	
	
	/**
	 * AccessToken 발급
	 */
//	@Test
	public void AccessToken_발급() throws Exception {
		// given
		String accessToken = jwtService.createAccessToken(email);
		
		DecodedJWT verify = getVerify(accessToken);
		
		String subject = verify.getSubject();
		String findEmail = verify.getClaim(EMAIL_CLAIM).asString();
		
		// then
		assertThat(findEmail).isEqualTo(email);
		assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
	}

	
	
	/**
	 * RefreshToken 발급
	 */
//	@Test
	public void createRefreshToken_RefreshToken_발급() throws Exception {
		// given, when
		String refreshToken = jwtService.createRefreshToken();
		DecodedJWT verify = getVerify(refreshToken);
		String subject = verify.getSubject();
		String email = verify.getClaim(EMAIL_CLAIM).asString();
		
		// then
		assertThat(subject).isEqualTo(REFRESH_TOKEN_SUBJECT);
		assertThat(email).isNull();
	}
	
	
	
	/**
	 * RefreshToken 업데이트
	 */
//	@Test
	public void updateRefreshToken_refreshToken_업데이트() throws Exception {
		// given
		String refreshToken = jwtService.createRefreshToken();
		jwtService.updateRefreshToken(email, refreshToken);
		clear();
		Thread.sleep(3000); // 다음 토큰 발급과의 텀을 주기 위함
		
		// when
		String reIssuedRefreshToken = jwtService.createRefreshToken();
		jwtService.updateRefreshToken(email, reIssuedRefreshToken);
		clear();
		
		// then
		assertThrows(Exception.class, () -> memberRepository.findByRefreshToken(refreshToken).get());
		assertThat(memberRepository.findByRefreshToken(reIssuedRefreshToken).get().getEmail()).isEqualTo(email);
	}
	
	
	
	/**
	 * RefreshToken 제거
	 */
//	@Test
	public void destroyRefreshToken_refreshToken_제거() throws Exception {
		// given
		String refreshToken = jwtService.createRefreshToken();
		jwtService.updateRefreshToken(email, refreshToken); // refreshToken 발급
		clear();
		
		// when
		jwtService.destroyRefreshToken(email); // 토큰 파기
		clear();
		
		// then
		assertThrows(Exception.class, () -> memberRepository.findByRefreshToken(refreshToken).get());
		// refreshToken으로 회원 정보를 조회할 때, 해당 토큰이 더 이상 유효하지 않으므로 예외가 발생해야 함
		
		Member member = memberRepository.findByEmail(email).get(); // 유저 객체 받아옴
		assertThat(member.getRefreshToken()).isNull(); // 유저에게 토큰이 있는지 최종 확인
	}
	
	
	
	/**
	 * 토큰 유효성 검사
	 */
//	@Test
	public void 토큰_유효성_검사() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();

        //when, then
        assertThat(jwtService.isTokenValid(accessToken)).isTrue();
        assertThat(jwtService.isTokenValid(refreshToken)).isTrue();
    }
	
	
	
	/**
	 * AccessToken, RefreshToken 헤더 설정
	 */
	@Test
	public void setAccessTokenHeader_AccessToken_헤더_설정() throws Exception {
		// MockHttpServletResponse 객체 생성: 실제 HTTP 응답을 모방
		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
		
		String accessToken = jwtService.createAccessToken(email);
		String refreshToken = jwtService.createRefreshToken();
		
		jwtService.setAccessTokenHeader(mockHttpServletResponse, accessToken);
		
		// when
		jwtService.sendAccessAndRefreshToken(mockHttpServletResponse, accessToken, refreshToken);
		
		// then
		String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader); // 설정된 헤더에서 액세스 토큰 가져오기
		
		assertThat(headerAccessToken).isEqualTo(accessToken); // 응답 헤더의 액세스 토큰이 원래 생성한 액세스 토큰과 동일한지 검증
	}
	
	
	
//	@Test
    public void setRefreshTokenHeader_RefreshToken_헤더_설정() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.setRefreshTokenHeader(mockHttpServletResponse, refreshToken);

        //when
        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        //then
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        assertThat(headerRefreshToken).isEqualTo(refreshToken);
    }
	
	
	
	/**
	 * 토큰 전송 테스트
	 */
//    @Test
	public void sendToken_토큰_전송() throws Exception {
		// given
		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
		
		String accessToken = jwtService.createAccessToken(email);
		String refreshToken = jwtService.createRefreshToken();
		
		// when
		jwtService.sendAccessAndRefreshToken(mockHttpServletResponse, accessToken, refreshToken);
		
		// then
		String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);
		
        assertThat(headerAccessToken).isEqualTo(accessToken);
        assertThat(headerRefreshToken).isEqualTo(refreshToken);
	}
    
	
	
//	@Test
	public void extractAccessToken_AccessToken_추출() throws Exception {
		//given
		String accessToken = jwtService.createAccessToken(email);
		String refreshToken = jwtService.createRefreshToken();
		HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);
		
		//when
		String extractAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(() -> new Exception("토큰이 없습니다."));
		
		//then
		assertThat(extractAccessToken).isEqualTo(accessToken);
		assertThat(getVerify(extractAccessToken).getClaim(EMAIL_CLAIM).asString()).isEqualTo(email);
	}
    
    
    /**
     * RefreshToken 추출
     */
//	@Test
	public void extractRefreshToken_RefreshToken_추출() throws Exception {
		// given
		String accessToken = jwtService.createAccessToken(email);
		String refreshToken = jwtService.createRefreshToken();
		HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);
		
		// when
		String extractRefreshToken = jwtService.extractRefreshToken(httpServletRequest).orElseThrow(() -> new Exception("토큰이 없습니다."));
		
		// then
		assertThat(extractRefreshToken).isEqualTo(refreshToken); // 추출한 리프레시 토큰이 원래 생성한 리프레시 토큰과 동일한지 검증
		assertThat(getVerify(extractRefreshToken).getSubject()).isEqualTo(REFRESH_TOKEN_SUBJECT);
	}
	
	
	
	/**
	 * Username 추출
	 */
//	@Test
	public void extractUsername_Username_추출() throws Exception {
		// given
		String accessToken = jwtService.createAccessToken(email);
		String refreshToken = jwtService.createRefreshToken();
		HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);
		
		String requestAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(() -> new Exception("토큰이 없습니다."));
		
		// when
		String extractUsername = jwtService.extractEmail(requestAccessToken).orElseThrow(() -> new Exception("토큰이 없습니다.")); // username 추출
		
		// then
		assertThat(extractUsername).isEqualTo(email);
	}
}