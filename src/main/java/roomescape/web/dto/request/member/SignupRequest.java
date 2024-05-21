package roomescape.web.dto.request.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import roomescape.domain.Member;

public record SignupRequest(
        @NotBlank(message = "이름은 공백일 수 없습니다.") String name,
        @Email(message = "이메일 형식을 지켜주세요.") String email,
        @NotBlank(message = "비밀번호는 공백일 수 없습니다.") String password) {

    public Member toMember() {
        return new Member(name, email, password);
    }
}
