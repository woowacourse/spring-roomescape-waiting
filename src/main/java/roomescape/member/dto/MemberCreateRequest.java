package roomescape.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequest(
        @NotBlank
        String email,
        @NotBlank
        String password,
        @NotBlank
        String name
) {

}
