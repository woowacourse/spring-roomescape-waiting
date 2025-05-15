package roomescape.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberCreateDto(
        @NotNull
        @Email
        String email,
        @NotNull
        @NotBlank
        String password,
        @NotNull
        @NotBlank
        String name
) {
}
