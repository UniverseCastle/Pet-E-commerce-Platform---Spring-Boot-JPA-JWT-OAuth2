package project.shop.domain.member.entity;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.shop.domain.member.enums.Role;
import project.shop.domain.member.enums.SocialType;

@Entity
@Table(name = "MEMBER")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id; // PK
	
	@Email
	@Column(nullable = false, length = 30, unique = true)
	private String email; // 이메일
	
	private String password; // 비밀번호
	
	@Column(nullable = false, length = 30)
	private String name; // 이름(실명)
	
	@Column(nullable = false, length = 30, unique = true)
	private String nickName; // 별명
	
	@Column
	private String imageUrl; // 프로필 이미지
	
	@Column(nullable = false, length = 30)
	private int age; // 나이
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Role role; // 권한 | GUEST, USER, ADMIN |
	
	@Enumerated(EnumType.STRING)
	private SocialType socialType; // 소셜로그인 타입 | KAKAO, NAVER, GOOGLE |
	
	private String socialId; // 로그인한 소셜 타입의 식별자 값 (일반로그인: null)
	
	@Column(length = 1000)
	private String refreshToken; // 리프레시 토큰
	
	
	
	//== 정보 수정 ==//
	public void updatePassword(PasswordEncoder passwordEncoder, String password) {
		this.password = passwordEncoder.encode(password);
	}
	
	public void updateName(String name) {
		this.name = name;
	}
	
	public void updateNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public void updateAge(int age) {
		this.age = age;
	}
	
	//== 리프레시 토큰 ==//
	public void updateRefreshToken(String refreshToken) { // 리프레시 토큰 갱신
		this.refreshToken = refreshToken;
	}
	
	public void destroyRefreshToken() { // 리프레시 토큰 파기
		this.refreshToken = null;
	}
	
	
	
	//== 비밀번호 암호화 ==//
	public void encodePassword(PasswordEncoder passwordEncoder) {
		this.password = passwordEncoder.encode(password);
	}
	
	/**
	 * 비밀번호 일치하는지 확인
	 * @param : paswordEncoder 패스워드 인코더
	 * @param : checkPassword 검사할 비밀번호
	 * @return : true/false 리턴
	 */
	public boolean matchPassword(PasswordEncoder passwordEncoder, String checkPassword) {
		return passwordEncoder.matches(checkPassword, getPassword());
	}
	
	
	
	//== 권한 부여 ==//
	public void addUserAuthority() {
		this.role = Role.USER;
	}
}