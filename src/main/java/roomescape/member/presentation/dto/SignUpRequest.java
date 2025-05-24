package roomescape.member.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public class SignUpRequest {
    @Email
    private final String email;
    @NotBlank
    private final String password;
    @NotBlank
    private final String name;

    public SignUpRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public Member toUserMember() {
        return new Member(
                email,
                password,
                name,
                Role.USER
        );
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}
