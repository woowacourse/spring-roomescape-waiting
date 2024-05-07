package roomescape.service.dto;

import roomescape.controller.request.LoginRequest;
import roomescape.model.member.Email;
import roomescape.model.member.Password;

public class AuthDto {

    private final Email email;
    private final Password password;

    public AuthDto(String email, String password) {
        this.email = new Email(email);
        this.password = new Password(password);
    }

    public static AuthDto from(LoginRequest loginRequest) {
        return new AuthDto(loginRequest.getEmail(), loginRequest.getPassword());
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }
}
