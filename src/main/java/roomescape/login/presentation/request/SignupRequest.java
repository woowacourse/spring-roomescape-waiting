package roomescape.login.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.member.business.domain.Member;

public record SignupRequest(
        @Email String email,
        @NotBlank String password,
        @NotBlank String name
) {

    public Member toMember() {
        return new Member(name, email, password);
    }
}
