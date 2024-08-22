package project.shop.domain.member.service;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.AssertTrue;
import lombok.RequiredArgsConstructor;
import project.shop.domain.member.dto.MemberInfoDto;
import project.shop.domain.member.dto.MemberSignUpDto;
import project.shop.domain.member.dto.MemberUpdateDto;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.exception.MemberException;
import project.shop.domain.member.exception.MemberExceptionType;
import project.shop.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	
	
	/**
	 * [회원가입 메서드]
	 * 
	 * 유효성 검사 후 회원가입 진행
	 */
	@Override
	public void signUp(MemberSignUpDto memberSignUpDto) throws Exception {
		
		Member member = memberSignUpDto.toEntity(); // 빌더패턴으로 회원객체에 저장
		
		member.addUserAuthority(); // entity로 변환 후 USER 권한 부여
		member.encodePassword(passwordEncoder); // 비밀번호 암호화
		
		// 닉네임 중복검사
		if (memberRepository.findByNickName(memberSignUpDto.nickName()).isPresent()) {
			throw new MemberException(MemberExceptionType.ALREADY_EXIST_NICKNAME);
		}
		// 이메일 중복검사
		if (memberRepository.findByEmail(memberSignUpDto.email()).isPresent()) {
			throw new MemberException(MemberExceptionType.ALREADY_EXIST_EMAIL);
		}
		// 생년월일 범위 검사
		if (!isBirthDateValid(memberSignUpDto.birth())) {
			throw new MemberException(MemberExceptionType.BIRTH_DATE_ERROR);
		}
		// 두 비밀번호가 일치하는지 검사
		if (!memberSignUpDto.password1().equals(memberSignUpDto.password2())) {
			throw new MemberException(MemberExceptionType.PASSWORD_MISMATCH);
		}
		
		memberRepository.save(member);
	}
	
	
	
	/**
	 * [회원가입 생년월일 범위 체크 메서드]
	 * 
	 * @param birth
	 * 회원가입 시 입력받은 생년월일
	 * 
	 * @return
	 * 13세 이상, 120세 이하 인 경우 true
	 */
	private boolean isBirthDateValid(LocalDate birth) {
		if (birth == null) {
			return false;
		}
		int age = Period.between(birth, LocalDate.now()).getYears();
		
		return age >= 13 && age <= 120;
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