package project.shop.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import project.shop.domain.member.dto.MemberSignUpDto;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.enums.Role;
import project.shop.domain.member.repository.MemberRepository;

@SpringBootTest
@Transactional
@ActiveProfiles(profiles = "dev-profile")
class MemberServiceTest {

	@Autowired
	EntityManager em;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	String PASSWORD = "password";
	
	private void clear() {
		em.flush();
		em.clear();
	}
	
	private MemberSignUpDto makeMemberSignUpDto() {
		return new MemberSignUpDto("spring@gmail.com", PASSWORD, "name", "nickName", 20);
	}
	
	private MemberSignUpDto setMember() throws Exception {
		MemberSignUpDto memberSignUpDto = makeMemberSignUpDto();
		memberService.signUp(memberSignUpDto);
		clear();
		
		SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
		
		emptyContext.setAuthentication(new UsernamePasswordAuthenticationToken(User.builder()
				.username(memberSignUpDto.email())
				.password(memberSignUpDto.password())
				.roles(Role.USER.name())
				.build(),
				null, null)
		);
		
		SecurityContextHolder.setContext(emptyContext);
		
		return memberSignUpDto;
	}
	
	@AfterEach
	public void removeMember() {
		SecurityContextHolder.createEmptyContext().setAuthentication(null);
	}
	
//	@Test
	public void signUp() throws Exception {
		// given
		MemberSignUpDto memberSignUpDto = makeMemberSignUpDto();
		
		// when
		memberService.signUp(memberSignUpDto);
		clear();
		
		// then
		Member member = memberRepository.findByEmail(memberSignUpDto.email()).orElseThrow(() -> new Exception());
		
		assertThat(member.getId()).isNotNull();
		assertThat(member.getEmail()).isEqualTo(memberSignUpDto.email());
		assertThat(member.getName()).isEqualTo(memberSignUpDto.name());
		assertThat(member.getNickName()).isEqualTo(memberSignUpDto.nickName());
		assertThat(member.getAge()).isEqualTo(memberSignUpDto.age());
		assertThat(member.getRole()).isSameAs(Role.USER);
	}
}