package roomescape.application.dto.request.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import roomescape.domain.member.Member;

public record SignupRequest(
        @NotBlank(message = "이름은 공백일 수 없습니다.") String name,
        @Pattern(
                regexp = "^[a-zA-Z0-9+._-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
                message = "이메일 또는 비밀번호를 형식에 맞춰 입력해주세요."
        ) String email,
        @NotBlank(message = "비밀번호는 공백일 수 없습니다.") String password
) {

    public Member toMember() {
        return new Member(name, email, password);
    }
}
