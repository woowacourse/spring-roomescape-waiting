package roomescape.member.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(
        @NotBlank @Size(max = 20) String name,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
