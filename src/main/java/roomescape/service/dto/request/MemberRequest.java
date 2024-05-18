package roomescape.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.Member;
import roomescape.domain.Role;

public record MemberRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        String name,
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "email형식에 맞지 않습니다.")
        String email,
        @NotBlank(message = "패스워드를 입력해주세요.")
        String password,
        @NotNull(message = "역할을 입력해주세요.")
        Role role) {
    public Member toEntity() {
        return new Member(name, email, password, role);
    }

}
