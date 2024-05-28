package roomescape.controller.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.Role;

public record LoginMember(
        @NotNull
        Long id,

        @NotBlank
        String name,

        @NotNull
        Role role) {

    public boolean isAdmin() {
        return Role.ADMIN == role;
    }
}
