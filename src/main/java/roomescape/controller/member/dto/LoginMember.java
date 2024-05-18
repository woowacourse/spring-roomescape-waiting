package roomescape.controller.member.dto;

import jakarta.validation.constraints.NotNull;

public record LoginMember(
        @NotNull
        Long id) {
}
