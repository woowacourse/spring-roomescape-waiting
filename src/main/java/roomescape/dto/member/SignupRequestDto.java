package roomescape.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import roomescape.domain.member.Reserver;
import roomescape.domain.member.Role;

public record SignupRequestDto(@NotNull @Email(message = "이메일 형식이 아닙니다") String email,
                               @NotNull @Size(min = 4, max = 8, message = "패스워드는 4-8 글자입니다") String password,
                               @NotNull @Size(min = 2, max = 10, message = "이름은 최소 2글자 최대 10글자 입니다") String name) {

    public Reserver toEntity() {
        return new Reserver(null, email, password, name, Role.USER);
    }
}
