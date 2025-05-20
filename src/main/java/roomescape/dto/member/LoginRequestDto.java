package roomescape.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import roomescape.domain.member.Reserver;
import roomescape.domain.member.Role;

public record LoginRequestDto(@NotNull @Email(message = "이메일 형식이 아닙니다") String email,
                              @NotNull @Size(min = 4, max = 8, message = "패스워드는 4-8 글자입니다") String password) {

    public Reserver toEntity() {
        return new Reserver(null, email, password, null, Role.USER);
    }
}
