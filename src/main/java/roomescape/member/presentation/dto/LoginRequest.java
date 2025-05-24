package roomescape.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String password, @NotBlank String email) {

}
