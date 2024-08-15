package project.shop.global.oauth2.userinfo;

import java.util.Map;

public abstract class OAuth2UserInfo {

	protected Map<String, Object> attributes; // 추상클래스를 상속받는 클래스에서만 사용할 수 있도록 protected 제어자 사용
	
	public OAuth2UserInfo(Map<String, Object> attributes) { // 생성자 파라미터로 각 소셜타입 별 유저정보 attributes를 주입받아
		this.attributes = attributes;						// 소셜타입에 맞는 attributes를 주입받도록 함
	}
	
	
	
	public abstract String getId(); // 소셜 식별값: 구글 - "sub", 카카오 - "id", 네이버 - "id"
	
	public abstract String getNickname();
	
	public abstract String getImageUrl();
	
	// 소셜 별로 제공하는 정보 추가 가능
}