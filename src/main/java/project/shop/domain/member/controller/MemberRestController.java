package project.shop.domain.member.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import project.shop.domain.member.dto.MemberLoginDto;
import project.shop.domain.member.dto.MemberSignUpDto;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.repository.MemberRepository;
import project.shop.domain.member.service.MemberService;
import project.shop.domain.member.util.ValidationSequence;
import project.shop.global.login.service.LoginService;

@RestController // 문자열을 response의 Body에 작성하여 전속
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberRestController {
	
	private final MemberRepository memberRepository;
	private final MemberService memberService;
	private final LoginService loginService;
	
	

	/**
	 * [회원가입]
	 * 
	 * @param memberSignUpDto
	 * 회원가입 시 받은 정보
	 */
	@PostMapping("/signUp")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Map<String, String>> signUp(@Validated(ValidationSequence.class) @RequestBody
													  MemberSignUpDto memberSignUpDto, BindingResult bindingResult) throws Exception {
		
		Map<String, String> errors = new HashMap<>();
		
		// 에러 발생 시 에러 메세지 리턴 (뷰에서 보여줄 메세지)
		if (bindingResult.hasErrors()) {
			bindingResult.getFieldErrors().forEach(error -> { // 모든 에러 메세지 반복해서 꺼냄
				errors.put(error.getField(), error.getDefaultMessage());
			});
			return ResponseEntity.badRequest().body(errors);
		}
		
		// 가입 로직 처리
		memberService.signUp(memberSignUpDto);
		
		return ResponseEntity.ok(Collections.singletonMap("message", "회원가입이 완료되었습니다."));
	}
	
	/**
	 * OAuth2 회원가입
	 */
	@PostMapping("/oauth2/signUp")
	@ResponseStatus(HttpStatus.OK)
	public void oauthSignUp() {
		
	}
	
	/**
	 * 일반 회원 로그인
	 */
	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	public void login(@RequestBody MemberLoginDto memberLoginDto) {
		Optional<Member> omember = memberRepository.findByEmail(memberLoginDto.email());
		if (omember.isPresent()) {
			Member member = omember.get();
			loginService.loadUserByUsername(member.getEmail());
		}
	}
}
