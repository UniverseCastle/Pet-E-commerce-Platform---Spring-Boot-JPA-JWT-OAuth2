package project.shop.domain.member.dto;

import lombok.Builder;
import lombok.Data;
import project.shop.domain.member.entity.Member;

@Data
public class MemberInfoDto {

	private final String email;
	private final String name;
	private final String nickName;
//	private final Integer age;
	
	// 기본 생성자 추가
//    public MemberInfoDto() {
//    	this.email = null;
//        this.name = null;
//        this.nickName = null;
//        this.age = null;
//    }
	
	@Builder
	public MemberInfoDto(Member member) {
		this.email = member.getEmail();
		this.name = member.getName();
		this.nickName = member.getNickName();
//		this.age = member.getAge();
	}
}