package roomescape.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public record LoginMember(
        @NotNull
        Long id,
        @NotBlank
        String name,
        @Email
        String email,
        @NotNull
        Role role
) {

    public static LoginMember of(Member member) {
        return new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }
}
