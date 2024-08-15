package project.shop.global.login.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.exception.MemberException;
import project.shop.domain.member.exception.MemberExceptionType;
import project.shop.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER));
		
		return User.builder()
				.username(member.getEmail())
				.password(member.getPassword())
				.roles(member.getRole().name())
				.build();
	}
}