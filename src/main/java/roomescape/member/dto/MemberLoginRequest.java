package roomescape.member.dto;

import jakarta.validation.constraints.NotNull;

public record MemberLoginRequest(
        @NotNull
        String email,
        @NotNull
        String password
) {

}
