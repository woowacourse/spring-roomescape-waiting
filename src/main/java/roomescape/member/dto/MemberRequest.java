package roomescape.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record MemberRequest(
        @Email String email,
        @NotNull String password,
        @NotNull String name
) {
}
