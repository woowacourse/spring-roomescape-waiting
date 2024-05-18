package roomescape.controller.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String name) {
}
