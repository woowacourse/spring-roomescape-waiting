package roomescape.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.member.domain.Member;

public record SignUpRequest(
        @NotBlank(message = "회원명은 null 또는 공백일 수 없습니다.")
        @Size(min = 1, max = 50, message = "회원명은 null 또는 공백일 수 없습니다.")
        String name,
        @NotBlank(message = "이메일은 null 또는 공백일 수 없습니다.")
        @Email(message = "이메일 형식이 일치하지 않습니다. (xxx@xxx.xxx)")
        String email,
        @NotBlank(message = "비밀번호는 null 또는 공백일 수 없습니다.")
        String password
) {
    public Member toMemberEntity() {
        return new Member(
                name,
                email,
                password
        );
    }

}
