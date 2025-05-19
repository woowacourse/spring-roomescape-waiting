package roomescape.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String password, @NotBlank String email) {

}
