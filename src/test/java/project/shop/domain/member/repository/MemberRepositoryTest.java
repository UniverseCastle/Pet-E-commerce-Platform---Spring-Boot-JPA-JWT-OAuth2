package project.shop.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.enums.Role;
import project.shop.domain.member.exception.MemberException;
import project.shop.domain.member.exception.MemberExceptionType;

@SpringBootTest
@Transactional
@ActiveProfiles(profiles = "dev-profile")
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	EntityManager em;
	
	@AfterEach
	private void after() {
		em.clear();
	}
	
	void clear() {
		em.flush();
		em.clear();
	}
	
	
	
	//== TEST ==//
	
	
	
//	@Test
//	public void 회원저장() throws Exception {
//		// given
//		Member member = Member.builder()
//				.email("spring@gmail.com")
//				.password("1234567890")
//				.name("Member1")
//				.nickName("NickName1")
//				.role(Role.USER)
//				.age(20)
//				.build();
//		
//		// when
//		Member saveMember = memberRepository.save(member);
//		
//		// then
//		Member findMember = memberRepository.findById(saveMember.getId()).orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));
//		
//		assertThat(findMember).isSameAs(saveMember);
//		assertThat(findMember).isSameAs(member);
//	}

//	@Test
//	public void 이메일없음() throws Exception {
//		// given
//		Member member = Member.builder()
//				.password("1234567890")
//				.name("Member1")
//				.nickName("NickName1")
//				.role(Role.USER)
//				.age(20)
//				.build();
//		
//		// when, then
//		assertThrows(Exception.class, () -> memberRepository.save(member));
//	}
	
//	@Test
//	public void 중복이메일() throws Exception {
//		// given
//		Member member1 = Member.builder()
//				.email("spring@gmail.com")
//				.password("1234567890")
//				.name("Member1")
//				.nickName("NickName1")
//				.role(Role.USER)
//				.age(20)
//				.build();
//		
//		Member member2 = Member.builder()
//				.email("spring@gmail.com")
//				.password("1234567890")
//				.name("Member2")
//				.nickName("NickName2")
//				.role(Role.USER)
//				.age(20)
//				.build();
//		
//		memberRepository.save(member1);
//		clear();
//		
//		// when, then
//		assertThrows(Exception.class, () -> memberRepository.save(member2));
//	}
}