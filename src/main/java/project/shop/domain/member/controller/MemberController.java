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
	
	
	
	/**
	 * 일반 회원가입
	 */
	@GetMapping("/signUp")
	public String signUp() {
		
		return "member/signUp";
	}
	
	/**
	 * 소셜 로그인
	 */
	@GetMapping("/oauth2/signUp")
	public String oauth2SignUp() {
		
		return "member/oauth2SignUp";
	}
	
	/**
	 * 로그인 허브
	 */
	@GetMapping("/loginHub")
	public String loginHub() {
		
		return "member/loginHub";
	}
	
	/**
	 * 비회원 주문 조회
	 */
	@GetMapping("/guestSearchOrder")
	public String guestSearchOrder() {
		
		return "member/guestSearchOrder";
	}
}