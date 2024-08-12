package project.shop.domain.member.exception;

import project.shop.global.exception.BaseException;
import project.shop.global.exception.BaseExceptionType;

public class MemberException extends BaseException {

	private BaseExceptionType exceptionType;
	
	public MemberException(BaseExceptionType exceptionType) {
		this.exceptionType = exceptionType;
	}

	@Override
	public BaseExceptionType getExceptionType() {
		return exceptionType;
	}
}