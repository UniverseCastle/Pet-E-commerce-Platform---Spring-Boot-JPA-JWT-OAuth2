package project.shop.global.oauth2.userinfo;

import java.util.Map;

/**
 * 네이버는 attributes를 받았을 때 바로 유저정보가 있는게 아니라
 * 'response' Key로 한 번 감싸져있기 때문에
 * get("response")로 꺼낸 후
 * 사용할 정보 Key로 꺼내서 사용
 * 또한 get으로 꺼내면 Object로 반환되기 때문에 String으로 캐스팅하여 반환해야함
 */
public class NaverOAuth2UserInfo extends OAuth2UserInfo {

	public NaverOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
	}

	
	
	@Override
	public String getId() {
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		
		if (response == null) {
			return null;
		}
		
		return (String) response.get("id");
	}

	@Override
	public String getNickname() {
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		
		if (response == null) {
			return null;
		}
		
		return (String) response.get("nickname");
	}

	@Override
	public String getImageUrl() {
		Map<String, Object> response = (Map<String, Object>) attributes.get("response");
		
		if (response == null) {
			return null;
		}
		
		return (String) response.get("profile_image");
	}
}

/* 네이버 유저정보 Response Json 예시
{
  "resultcode": "00",
  "message": "success",
  "response": {
    "email": "openapi@naver.com",
    "nickname": "OpenAPI",
    "profile_image": "https://ssl.pstatic.net/static/pwe/address/nodata_33x33.gif",
    "age": "40-49",
    "gender": "F",
    "id": "32742776",
    "name": "오픈 API",
    "birthday": "10-01"
  }
}
*/