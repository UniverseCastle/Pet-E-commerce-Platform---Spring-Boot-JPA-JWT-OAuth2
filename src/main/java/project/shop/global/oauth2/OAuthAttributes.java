package project.shop.global.oauth2;

import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.enums.Role;
import project.shop.domain.member.enums.SocialType;
import project.shop.global.oauth2.userinfo.KakaoOAuth2UserInfo;
import project.shop.global.oauth2.userinfo.OAuth2UserInfo;

/**
 * 각 소셜에서 받아오는 데이터가 다르므로
 * 소셜별로 받는 데이터를 분기 처리하는 DTO 클래스
 */
@Getter
public class OAuthAttributes {

	private String nameAttributeKey; // OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 같은 의미
	private OAuth2UserInfo oauth2UserInfo; // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 사진 등)
	
	@Builder
	private OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oauth2UserInfo) {
		this.nameAttributeKey = nameAttributeKey;
		this.oauth2UserInfo = oauth2UserInfo;
	}
	
	
	
	/**
	 * @param socialType에 맞는 메소드 호출하여 OAuthAttributes 객체 반환
     * @param userNameAttributeName -> OAuth2 로그인 시 키(PK)가 되는 값
     * @param attributes : OAuth 서비스의 유저 정보들
     * 
     * 소셜별 of 메소드(ofGoogle, ofKaKao, ofNaver)들은 각각 소셜 로그인 API에서 제공하는
     * 회원의 식별값(id), attributes, nameAttributeKey를 저장 후 build
	 */
	public static OAuthAttributes of(SocialType socialType, String userNameAttributeName, Map<String, Object> attributes) {
		if (socialType == SocialType.NAVER) {
			return ofNaver(userNameAttributeName, attributes);
		}
		if (socialType == SocialType.KAKAO) {
			return ofKakao(userNameAttributeName, attributes);
		}
		
		return ofGoogle(userNameAttributeName, attributes);
	}
	
	
	
	private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
		return OAuthAttributes.builder()
				.nameAttributeKey(userNameAttributeName)
				.oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
				.build();
	}
	
	private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
		return OAuthAttributes.builder()
				.nameAttributeKey(userNameAttributeName)
				.oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
				.build();
	}
	
	private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
		return OAuthAttributes.builder()
				.nameAttributeKey(userNameAttributeName)
				.oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
				.build();
	}
	
	
	
	/**
	 * of 메서드로 OAuthAttributes 객체가 생성되어 유저 정보가 담긴 OAuth2UserInfo가
	 * @param socialType별로 주입된 상태
	 * 
	 * @param oauth2UserInfo 에서 socialId(식별값), nickname, imageUrl을 가져와서 build
	 * 
	 * email에는 UUID로 중복 없는 랜덤값 생성
	 * role은 GUEST로 설정
	 */
	public Member toEntity(SocialType socialType, OAuth2UserInfo oauth2UserInfo) {
		return Member.builder()
				.socialType(socialType)
				.socialId(oauth2UserInfo.getId())
				.email(UUID.randomUUID() + "@socialUser.com")
				.nickName(oauth2UserInfo.getNickname())
				.imageUrl(oauth2UserInfo.getImageUrl())
				.role(Role.GUEST)
				.build();
	}
}