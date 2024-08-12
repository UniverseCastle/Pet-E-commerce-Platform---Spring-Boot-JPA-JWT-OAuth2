package project.shop.domain.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.shop.domain.member.dto.MemberInfoDto;
import project.shop.domain.member.dto.MemberSignUpDto;
import project.shop.domain.member.dto.MemberUpdateDto;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	
	
	
	@Override // 회원가입
	public void signUp(MemberSignUpDto memberSignUpDto) throws Exception {
		Member member = memberSignUpDto.toEntity(); // 빌더패턴으로 회원객체에 저장
		
		member.addUserAuthority(); // entity로 변환 후 USER 권한 부여
		member.encodePassword(passwordEncoder); // 비밀번호 암호화
		
		// 중복검사
		if (memberRepository.findByEmail(memberSignUpDto.email()).isPresent()) {
			throw new Exception("이미 존재하는 아이디 입니다."); // TODO: 예외처리 만들기
		}
		if (memberRepository.findByNickName(memberSignUpDto.nickName()).isPresent()) {
			throw new Exception("이미 존재하는 닉네임 입니다.");
		}
		
		memberRepository.save(member);
	}
	
	
	@Override
	public void update(MemberUpdateDto memberUpdateDto) throws Exception {
		
	}
	@Override
	public void updatePassword(String checkPassword, String toBePassword) throws Exception {
		
	}
	@Override
	public void withdraw(String checkPassword) throws Exception {
		
	}
	@Override
	public MemberInfoDto getInfo(Long id) throws Exception {
		return null;
	}
	@Override
	public MemberInfoDto getMyInfo() throws Exception {
		return null;
	}
	
	
}