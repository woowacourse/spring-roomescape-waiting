package roomescape.auth.ui.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAccessTokenRequest(
        @NotBlank
        String email,
        @NotBlank
        String password
) {

}
