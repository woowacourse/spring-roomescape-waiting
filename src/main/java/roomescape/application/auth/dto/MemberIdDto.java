package roomescape.application.auth.dto;

import jakarta.validation.constraints.NotNull;

public record MemberIdDto(
        @NotNull Long id
) {
}
