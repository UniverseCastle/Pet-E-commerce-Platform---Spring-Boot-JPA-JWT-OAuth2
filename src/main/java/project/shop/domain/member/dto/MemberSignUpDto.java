package project.shop.domain.member.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import project.shop.domain.member.entity.Member;
import project.shop.domain.member.util.ValidationGroups.NotBlankGroup;
import project.shop.domain.member.util.ValidationGroups.PatternGroup;
import project.shop.domain.member.util.ValidationGroups.SizeGroup;

/**
 * 자체 회원가입 폼
 */
public record MemberSignUpDto(@NotBlank(message = "이름을 입력해주세요.", groups = NotBlankGroup.class)
							  @Pattern(regexp = "^[A-Za-z가-힣]+$", message = "사용자 이름은 한글 또는 알파벳만 입력해주세요.", groups = PatternGroup.class)
							  @Size(min = 2, message = "이름은 2~30자 이내로 입력해주세요.", groups = SizeGroup.class)
							  String name,
							  
							  @NotBlank(message = "닉네임을 입력해주세요.", groups = NotBlankGroup.class)
							  @Size(min = 2, message = "닉네임은 2~30자 이내로 입력해주세요.", groups = SizeGroup.class)
							  String nickName,
							  
							  @NotBlank(message = "이메일을 입력해주세요.", groups = NotBlankGroup.class)
							  @Size(min = 4, max = 25, message = "이메일은 4~25자 이내로 입력해주세요.", groups = SizeGroup.class)
							  String email,
							  
							  @NotBlank(message = "비밀번호를 입력해주세요.", groups = NotBlankGroup.class)
							  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,30}$",
							  message = "비밀번호는 8~30 자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다.", groups = PatternGroup.class)
							  String password1,
							  
							  @NotBlank(message = "비밀번호를 입력해주세요.", groups = NotBlankGroup.class)
							  String password2,
							  
							  @NotNull(message = "생년월일을 입력해주세요.", groups = NotBlankGroup.class)
							  @Past(message = "생년월일은 과거의 날짜여야 합니다.")
							  LocalDate birth,
							  
							  @NotBlank(message = "전화번호를 입력해주세요.", groups = NotBlankGroup.class)
							  @Size(min = 10, max = 11, message = "전화번호는 10자~11자 이내로 입력해주세요.", groups = SizeGroup.class)
							  String phone
							  
//							  @NotNull(message = "나이를 입력해주세요.")
//							  @Range(min = 0, max = 150)
//							  Integer age
							  ) {

	public Member toEntity() {
		return Member.builder()
				.name(name)
				.nickName(nickName)
				.email(email)
				.password(password1)
				.birth(birth)
				.phone(phone)
//				.age(age)
				.build();
	}
}