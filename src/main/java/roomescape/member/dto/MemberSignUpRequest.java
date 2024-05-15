package roomescape.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.member.domain.Member;

public record MemberSignUpRequest(
        @NotNull @NotBlank String name,
        @NotNull @Email String email,
        @NotNull @NotBlank String password
) {

    public Member toMember() {
        return new Member(name, email, password);
    }
}
