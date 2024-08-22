package project.shop.domain.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import project.shop.domain.member.service.MemberService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

	private final MemberService memberService;
	
	
	
	@GetMapping("/signUp")
	public String signUp() {
		
		return "member/signUp";
	}
	
	@GetMapping("/oauth2/signUp")
	public String oauth2SignUp() {
		
		return "member/oauth2SignUp";
	}
}