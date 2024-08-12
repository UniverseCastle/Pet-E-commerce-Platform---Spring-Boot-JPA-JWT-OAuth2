package project.shop.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import project.shop.domain.member.entity.Member;
import project.shop.domain.member.enums.SocialType;

public interface MemberRepository extends JpaRepository<Member, Long> {
	// 소셜 로그인으로 반환되는 값 중 email을 통해 이미 생성된 사용자인지, 처음 가입하는 사용자인지 판단하기 위한 메서드
	Optional<Member> findByEmail(String email);
	
	boolean existsByEmail(String email); // 이메일 존재 여부
	
	Optional<Member> findByNickName(String nickName);
	
	Optional<Member> findByRefreshToken(String refreshToken);
	
	/**
	 * 소셜 타입과 소셜의 식별값으로 회원을 찾는 메서드
	 * 정보 제공에 동의한 순간 DB에 저장되어야 하지만, 추가정보를 입력받지 않았으므로
	 * 유저 객체는 추가정보가 빠진 상태로 DB에 저장됨
	 * 따라서 추가정보를 입력받아 회원가입을 진행할 때 소셜타입, 식별자로 해당 회원을 찾기위함
	 */
	Optional<Member> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}