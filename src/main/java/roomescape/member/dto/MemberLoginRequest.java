package roomescape.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberLoginRequest(@NotNull @NotBlank String email, @NotNull @NotBlank String password) {
}
