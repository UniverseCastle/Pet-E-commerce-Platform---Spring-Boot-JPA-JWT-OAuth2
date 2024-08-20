package project.shop.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import project.shop.domain.member.dto.MemberSignUpDto;
import project.shop.domain.member.service.MemberService;

@RestController // 문자열을 response의 Body에 작성하여 전속
@RequiredArgsConstructor
public class MemberRestController {
	
	private final MemberService memberService;
	
	

	/**
	 * 회원가입
	 * @param memberSignUpDto
	 */
	@PostMapping("/signUp")
	@ResponseStatus(HttpStatus.OK)
	public void signUp(@Valid @RequestBody MemberSignUpDto memberSignUpDto) throws Exception {
		memberService.signUp(memberSignUpDto);
	}
	
	/**
	 * OAuth2 회원가입
	 */
	@PostMapping("/oauth2/signUp")
	@ResponseStatus(HttpStatus.OK)
	public void oauthSignUp() {
		
	}
}
