package project.shop.domain.member.service;

import project.shop.domain.member.dto.MemberInfoDto;
import project.shop.domain.member.dto.MemberSignUpDto;
import project.shop.domain.member.dto.MemberUpdateDto;

/**
 * 회원가입
 * 정보수정
 * 회원탈퇴
 * 정보조회
 */
public interface MemberService {
	
	void signUp(MemberSignUpDto memberSignUpDto) throws Exception;
	
	void update(MemberUpdateDto memberUpdateDto) throws Exception;
	
	// toBePassword: 바꿀 비밀번호
	void updatePassword(String checkPassword, String toBePassword) throws Exception;
	
	void withdraw(String checkPassword) throws Exception;
	
	MemberInfoDto getInfo(Long id) throws Exception;
	
	MemberInfoDto getMyInfo() throws Exception;
	
}