package roomescape.service.dto;

import roomescape.controller.request.LoginRequest;
import roomescape.model.member.MemberEmail;
import roomescape.model.member.MemberPassword;

public class AuthDto {

    private final MemberEmail email;
    private final MemberPassword password;

    public AuthDto(String email, String password) {
        this.email = new MemberEmail(email);
        this.password = new MemberPassword(password);
    }

    public MemberEmail getEmail() {
        return email;
    }

    public MemberPassword getPassword() {
        return password;
    }
}
