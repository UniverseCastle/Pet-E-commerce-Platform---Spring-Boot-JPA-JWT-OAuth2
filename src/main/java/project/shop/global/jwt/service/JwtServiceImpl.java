package project.shop.global.jwt.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import project.shop.domain.member.exception.MemberException;
import project.shop.domain.member.exception.MemberExceptionType;
import project.shop.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Setter(value = AccessLevel.PRIVATE)
@Getter
@Transactional
@Slf4j
public class JwtServiceImpl implements JwtService {

	/**
	 * {@value}
	 * yml 파일에 작성해둔 설정 값들을 가져와서 사용
	 * static으로 선언시 값이 들어오지 않음
	 */
	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${jwt.access.expiration}")
	private long accessTokenValidityInSeconds;
	
	@Value("${jwt.refresh.expiration}")
	private long refreshTokenValidityInSeconds;
	
	@Value("${jwt.access.header}")
	private String accessHeader;
	
	@Value("${jwt.refresh.header}")
	private String refreshHeader;
	
	/**
	 * JWT의 Subject와 Claim으로 email 사용 -> 클레임의 name을 "email"로 설정
	 * JWT의 헤더에 들어오는 값 : 'Authorization(Key) = Bearer {토큰} (Value)' 형식
	 */
	private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
	private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
	private static final String EMAIL_CLAIM = "email";
	private static final String BEARER = "Bearer ";
	
	private final MemberRepository memberRepository;

	
	
	/**
	 * AccessToken 생성 메서드
	 */
	@Override
	public String createAccessToken(String email) {
		return JWT.create() // JWT 토큰을 생성하는 빌더 반환
				.withSubject(ACCESS_TOKEN_SUBJECT) // JWT의 Subject 지정 -> AccessToken이므로 AccessToken
				.withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidityInSeconds * 1000)) // 토큰 만료 시간 설정
				.withClaim(EMAIL_CLAIM, email) // 식별자나 이름 등의 정보를 더 추가 가능, .withClaim(클래임 이름, 클래임 값)으로 설정해주면 됨
				.sign(Algorithm.HMAC512(secret)); // HMAC512 알고리즘 사용, application-jwt.yml에서 지정한 secret키로 암호화
	}

	/**
	 * RefreshToken 생성
	 * RefreshToken은 Claim에 email도 넣지 않으므로 withClaim() x
	 */
	@Override
	public String createRefreshToken() {
		return JWT.create()
				.withSubject(REFRESH_TOKEN_SUBJECT)
				.withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000))
				.sign(Algorithm.HMAC512(secret));
	}

	/**
	 * RefreshToken DB 저장(업데이트)
	 */
	@Override
	public void updateRefreshToken(String email, String refreshToken) {
		memberRepository.findByEmail(email)
			.ifPresentOrElse(
					member -> member.updateRefreshToken(refreshToken),
					() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER)
			);
	}

	/**
	 * RefreshToken 파기
	 */
	@Override
	public void destroyRefreshToken(String email) {
		memberRepository.findByEmail(email)
			.ifPresentOrElse(
					member -> member.destroyRefreshToken(),
					() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER)
			);
	}

	/**
	 * AccessToken + RefreshToken 헤더에 실어서 보내기
	 */
	@Override
	public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
		response.setStatus(HttpServletResponse.SC_OK);
		
		setAccessTokenHeader(response, accessToken);
		setRefreshTokenHeader(response, refreshToken);
		
		Map<String, String> tokenMap = new HashMap<>();
		tokenMap.put(ACCESS_TOKEN_SUBJECT, accessToken);
		tokenMap.put(REFRESH_TOKEN_SUBJECT, refreshToken);
		
		log.info("Access Token, Refresh Token 헤더 설정 완료");
	}

	/**
	 * AccessToken 헤더에 실어서 보내기
	 */
	@Override
	public void sendAccessToken(HttpServletResponse response, String accessToken) {
		response.setStatus(HttpServletResponse.SC_OK);
		
		setAccessTokenHeader(response, accessToken);
		
		Map<String, String> tokenMap = new HashMap<>();
		tokenMap.put(ACCESS_TOKEN_SUBJECT, accessToken);
		
		log.info("재발급된 Access Token : {}", accessToken);
	}

	/**
	 * 헤더에서 AccessToken 추출
	 * 토큰 형식 : Bearer xxx 에서 Bearer를 제외하고 순수 토큰만 가져오기 위해
	 * 헤더를 가져온 후 "Bearer "를 삭제(""로 replace)
	 */
	@Override
	public Optional<String> extractAccessToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(accessHeader)).filter(
				accessToken -> accessToken.startsWith(BEARER)
		// filter 통과한 엑세스 토큰에서 Bearer 문자열 제거
		).map(accessToken -> accessToken.replace(BEARER, "")); // 조건을 만족하는 경우 변환작업 수행
	}

	/**
	 * 헤더에서 RefreshToken 추출
	 * 토큰 형식 : Bearer xxx 에서 Bearer를 제외하고 순수 토큰만 가져오기 위해
	 * 헤더를 가져온 후 "Bearer "를 삭제(""로 replace)
	 */
	@Override
	public Optional<String> extractRefreshToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(refreshHeader)).filter(
				refreshToken -> refreshToken.startsWith(BEARER)
		).map(refreshToken -> refreshToken.replace(BEARER, ""));
	}

	/**
	 * AccessToken에서 Email 추출
	 * 추출 전에 JWT.require()로 검증기 생성
	 * verify로 AccessToken 검증 후
	 * 유효하지 않다면 빈 Optional 객체 반환
	 */
	@Override
	public Optional<String> extractEmail(String accessToken) {
		try {
			// 토큰 유효성 검사를 하는데에 사용할 알고리즘이 있는 JWT verifier builder 반환
			return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret))
					.build() // 반환된 빌더로 JWT verifier 생성
					.verify(accessToken) // accessToken을 검증하고, 유효하지 않다면 예외 발생
					.getClaim(EMAIL_CLAIM) // Claim(Email) 가져오기
					.asString());
		} catch (Exception e) {
			log.error("엑세스 토큰이 유효하지 않습니다.");
			
			return Optional.empty();
		}
	}

	/**
	 * AccessToken 헤더 설정
	 */
	@Override
	public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
		response.setHeader(accessHeader, accessToken);
	}

	/**
	 * RefreshToken 헤더 설정
	 */
	@Override
	public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
		response.setHeader(refreshHeader, refreshToken);
	}

	/**
	 * 토큰 유효성 검사
	 */
	@Override
	public boolean isTokenValid(String token) {
		try {
			JWT.require(Algorithm.HMAC512(secret)).build().verify(token);
			
			return true;
		} catch (Exception e) {
			log.error("유효하지 않은 토큰입니다.", e.getMessage());
			
			return false;
		}
	}
	
}