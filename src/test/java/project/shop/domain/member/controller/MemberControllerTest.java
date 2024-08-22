package project.shop.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import project.shop.domain.member.dto.MemberSignUpDto;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.repository.MemberRepository;
import project.shop.domain.member.service.MemberService;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "dev-profile")
class MemberControllerTest {
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	EntityManager em;
	
	@Autowired
	MockMvc mockMvc;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	PasswordEncoder passwordEncoder;

	private static String SIGN_UP_URL = "/signUp";
	
	private String email = "spring@gmail.com";
	private String password = "password1234@";
	private String name = "name";
	private String nickName = "nickName";
	private Integer age = 20;
	
	private void clear() { 
		em.flush();
		em.clear();
	}
	
	private void signUp(String signUpData) throws Exception {
		mockMvc.perform(
				post(SIGN_UP_URL)
					.contentType(MediaType.APPLICATION_JSON)
					.content(signUpData)
		).andExpect(status().isOk());
	}
	
//	회원가입
//	@Test
//	public void signUp() throws Exception {
//		// given
//		String signUpData = objectMapper.writeValueAsString(new MemberSignUpDto(email, password, name, nickName, age));
//		
//		// when
//		signUp(signUpData);
//		
//		// then
//		Member member = memberRepository.findByEmail(email).orElseThrow(() -> new Exception("회원이 존재하지 않습니다."));
//		
//		assertThat(member.getName()).isEqualTo(name);
//		assertThat(memberRepository.findAll().size()).isEqualTo(1);
//	}

}
