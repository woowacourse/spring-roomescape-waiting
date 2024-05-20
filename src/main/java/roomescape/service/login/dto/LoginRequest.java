package roomescape.service.login.dto;

import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberPassword;
import roomescape.exception.common.InvalidRequestBodyException;

public class LoginRequest {
    private final String email;
    private final String password;

    public LoginRequest(String email, String password) {
        validate(email, password);
        this.email = email;
        this.password = password;
    }

    private void validate(String email, String password) {
        if (email.isBlank() || password.isBlank()) {
            throw new InvalidRequestBodyException();
        }
    }

    public MemberEmail toMemberEmail() {
        return new MemberEmail(email);
    }

    public MemberPassword toMemberPassword() {
        return new MemberPassword(password);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
