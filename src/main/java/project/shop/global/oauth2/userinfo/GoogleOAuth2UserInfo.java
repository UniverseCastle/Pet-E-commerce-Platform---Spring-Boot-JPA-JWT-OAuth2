package project.shop.global.oauth2.userinfo;

import java.util.Map;

/**
 * 구글은 네이버, 카카오와는 달리 유저정보가 감싸져 있지 않음
 * 바로 get으로 유저정보 Key를 사용해서 꺼냄
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
	}

	
	
	@Override
	public String getId() {
		return (String) attributes.get("sub");
	}

	@Override
	public String getNickname() {
		return (String) attributes.get("name");
	}

	@Override
	public String getImageUrl() {
		return (String) attributes.get("picture");
	}
}

/* 구글 유저정보 Response Json 예시
{
   "sub": "식별값",
   "name": "name",
   "given_name": "given_name",
   "picture": "https//lh3.googleusercontent.com/~~",
   "email": "email",
   "email_verified": true,
   "locale": "ko"
}
*/