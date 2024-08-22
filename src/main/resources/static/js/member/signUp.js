/**
 * [회원가입]
 * 
 * button 클릭 이벤트 -> fetch로 submit
 * 
 */
document.getElementById('signUpForm').addEventListener('click', function(event) {
	event.preventDefault(); // 기본 폼 제출 방지
	
	const memberSignUpDto = {
		name: document.getElementById('name').value,
		nickName: document.getElementById('nickName').value,
		email: document.getElementById('email').value,
		password1: document.getElementById('password1').value,
		password2: document.getElementById('password2').value,
		birth: document.getElementById('birth').value,
		phone: document.getElementById('phone').value
	};
	
	fetch('/member/signUp', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(memberSignUpDto)
	})
	.then(response => {
		if (!response.ok) {
			return response.json().then(errors => {
				console.log('Error Response:', errors); // 오류 객체 로그
				showErrors(errors); // 뷰에서 표시할 에러 메세지
				
				if (errors.errorCode === 3000) { // 커스텀 에러 메세지가 있는 경우
					alert(errors.errorMessage);
				}
			});
		}
		
		return response.json(); // 성공인 경우 JSON 데이터 반환
	})
	.then(data => {
		alert(data.message); // 성공 메세지 표시
	})
	.catch(error => {
		console.error('Error: ', error); // 네트워크 오류 처리
	});
});
	
function showErrors(errors) {
	/* 모든 에러 메세지 공간 초기화 */
	document.getElementById('nameError').innerText = "";
	document.getElementById('nickNameError').innerText = "";
	document.getElementById('emailError').innerText = "";
	document.getElementById('password1Error').innerText = "";
	document.getElementById('password2Error').innerText = "";
	document.getElementById('birthError').innerText = "";
	document.getElementById('phoneError').innerText = "";
	
	/* 각 필드에 해당하는 에러 메세지 표시 */
	/* 이름 */
	if (errors.name) { // errors.name이 존재하는지 확인
		document.getElementById('nameError').innerText = errors.name; // p태그에 name 에러 메세지 표기
		/* 유효성 검사에 걸릴 시 input 테두리 색상 변경 */
		const inputName = document.getElementById('name'); // input id="name" 선택
		inputName.style.border = '1px solid red'; // 입력 박스에 빨간 테두리 추가 (오류가 있을 시 보여줄 테두리 색)
	}else { // 오류가 없을 경우
		const inputName = document.getElementById('name');
		inputName.style.border = ''; // 테두리 색상 초기화 (기본)
	}
	
	/* 닉네임 */
	if (errors.nickName) {
		document.getElementById('nickNameError').innerText = errors.nickName;
		
		const inputNickName = document.getElementById('nickName');
		inputNickName.style.border = '1px solid red';
	}else {
		const inputNickName = document.getElementById('nickName');
		inputNickName.style.border = '';
	}
	
	/* 이메일 */
	if (errors.email) {
		document.getElementById('emailError').innerText = errors.email;
		
		const inputEmail = document.getElementById('email');
		inputEmail.style.border = '1px solid red';
	}else {
		const inputEmail = document.getElementById('email');
		inputEmail.style.border = '';
	}
	
	/* 비밀번호 */
	if (errors.password1) {
		document.getElementById('password1Error').innerText = errors.password1;
		
		const inputPassword1 = document.getElementById('password1');
		inputPassword1.style.border = '1px solid red';
	}else {
		const inputPassword1 = document.getElementById('password1');
		inputPassword1.style.border = '';
	}
	
	/* 비밀번호 확인 */
	if (errors.password2) {
		document.getElementById('password2Error').innerText = errors.password2;
		
		const inputPassword2 = document.getElementById('password2');
		inputPassword2.style.border = '1px solid red';
	}else {
		const inputPassword2 = document.getElementById('password2');
		inputPassword2.style.border = '';
	}
	
	/* 생년월일 */
	if (errors.birth) {
		document.getElementById('birthError').innerText = errors.birth;
		
		const inputBirth = document.getElementById('birth');
		inputBirth.style.border = '1px solid red';
	}else {
		const inputBirth = document.getElementById('birth');
		inputBirth.style.border = '';
	}
	
	/* 전화번호 */
	if (errors.phone) {
		document.getElementById('phoneError').innerText = errors.phone;
		
		const inputPhone = document.getElementById('phone');
		inputPhone.style.border = '1px solid red';
	}else {
		const inputPhone = document.getElementById('phone');
		inputPhone.style.border = '';
	}
}