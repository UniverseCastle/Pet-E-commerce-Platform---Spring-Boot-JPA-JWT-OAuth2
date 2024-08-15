package project.shop.global.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.enums.Role;
import project.shop.domain.member.repository.MemberRepository;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "dev-profile")
class LoginTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	EntityManager em;
	
	PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	private static String KEY_EMAIL = "email";
	private static String KEY_PASSWORD = "password";
	private static String EMAIL = "spring@gmail.com";
	private static String PASSWORD = "password123@";
	
	private static String LOGIN_URL = "/login";
	
	@Value("${jwt.access.header}")
	private String accessHeader;
	
	@Value("${jwt.refresh.header}")
	private String refreshHeader;
	
	
	private void clear() {
		em.flush();
		em.clear();
	}
	
	@BeforeEach
	private void init() {
		memberRepository.save(Member.builder()
				.email(EMAIL)
				.password(delegatingPasswordEncoder.encode(PASSWORD))
				.name("Member1")
				.nickName("NickName1")
				.role(Role.USER)
				.age(20)
				.build());
		
		clear();
	}
	
	private Map getEmailPasswordMap(String email, String password) {
		Map<String, String> map = new HashMap<>();
		map.put(KEY_EMAIL, email);
		map.put(KEY_PASSWORD, password);
		
		return map;
	}
	
	private ResultActions perform(String url, MediaType mediaType, Map emailPasswordMap) throws Exception {
		return mockMvc.perform(MockMvcRequestBuilders
				.post(url)
				.contentType(mediaType)
				.content(objectMapper.writeValueAsString(emailPasswordMap)));
	}
	
	
	
	//== TEST ==//
	
	
//	@Test
	public void 로그인() throws Exception {
		// given
		Map<String, String> map = getEmailPasswordMap(EMAIL, PASSWORD);
		
		// when
		MvcResult result = perform(LOGIN_URL, MediaType.APPLICATION_JSON, map)
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn();
		
		// then
		assertThat(result.getResponse().getHeader(accessHeader)).isNotNull();
		assertThat(result.getResponse().getHeader(refreshHeader)).isNotNull();
	}

	
//	@Test
	public void 아이디틀림() throws Exception {
		// given
		Map<String, String> map = getEmailPasswordMap(EMAIL + "123", PASSWORD);
		
		// when
		MvcResult result = perform(LOGIN_URL, MediaType.APPLICATION_JSON, map)
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andReturn();
		
		// then
		assertThat(result.getResponse().getHeader(accessHeader)).isNull();
		assertThat(result.getResponse().getHeader(refreshHeader)).isNull();
	}
	
	
	
//	@Test
	public void 비번틀림() throws Exception {
		// given
		Map<String, String> map = getEmailPasswordMap(EMAIL, PASSWORD + "123");
		
		// when
		MvcResult result = perform(LOGIN_URL, MediaType.APPLICATION_JSON, map)
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andReturn();
		
		// then
		assertThat(result.getResponse().getHeader(accessHeader)).isNull();
		assertThat(result.getResponse().getHeader(refreshHeader)).isNull();
	}
	
	
//	@Test
	public void 로그인주소틀림() throws Exception {
		// given
		Map<String, String> map = getEmailPasswordMap(EMAIL, PASSWORD);
		
		// when, then
		perform(LOGIN_URL + "123", MediaType.APPLICATION_JSON, map)
				.andDo(print())
				.andExpect(status().isForbidden());
	}
	
	
//	@Test
	public void 로그인_데이터형식_JSON이_아니면_400() throws Exception {
        //given
        Map<String, String> map = getEmailPasswordMap(EMAIL, PASSWORD);

        //when, then
        perform(LOGIN_URL, MediaType.APPLICATION_FORM_URLENCODED, map)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }
	
	/*
	@Test
    public void 로그인_HTTP_METHOD_GET이면_NOTFOUND() throws Exception {
        //given
        Map<String, String> map = getEmailPasswordMap(EMAIL, PASSWORD);


        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    */
	
	/*
	@Test
	public void 오류_로그인_HTTP_METHOD_PUT이면_NOTFOUND() throws Exception {
        //given
        Map<String, String> map = getEmailPasswordMap(EMAIL, PASSWORD);


        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .put(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    */
}