package project.shop.domain.member.exception;

import org.springframework.http.HttpStatus;

import project.shop.global.exception.BaseExceptionType;

public enum MemberExceptionType implements BaseExceptionType{
	//== 회원가입, 로그인 시 ==//
	ALREADY_EXIST_EMAIL(600, HttpStatus.CONFLICT, "이미 존재하는 이메일 입니다."),
	ALREADY_EXIST_NICKNAME(601, HttpStatus.CONFLICT, "이미 존재하는 닉네임 입니다."),
	WRONG_PASSWORD(602, HttpStatus.BAD_REQUEST, "비밀번호가 잘못되었습니다."),
	NOT_FOUND_MEMBER(603, HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
	BIRTH_DATE_ERROR(604, HttpStatus.BAD_REQUEST, "생년월일 범위가 잘못되었습니다."),
	PASSWORD_MISMATCH(605, HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
//	PHONE_LENGTH_ERROR(605, HttpStatus.BAD_REQUEST, "전화번호 길이가 잘못되었습니다.");
	
	private int errorCode; // 에러코드
	private HttpStatus httpStatus; // Http 상태코드
	private String errorMessage; // 에러 메세지
	
	private MemberExceptionType(int errorCode, HttpStatus httpStatus, String errorMessage) {
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
		this.errorMessage = errorMessage;
	}
	
	

	@Override
	public int getErrorCode() {
		return this.errorCode;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return this.httpStatus;
	}

	@Override
	public String getErrorMessage() {
		return this.errorMessage;
	}

}