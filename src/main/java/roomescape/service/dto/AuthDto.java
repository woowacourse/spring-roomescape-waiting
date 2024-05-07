package roomescape.service.dto;

import roomescape.controller.request.LoginRequest;

public class AuthDto {

    private final String email;
    private final String password;

    public AuthDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static AuthDto from(LoginRequest loginRequest) {
        return new AuthDto(loginRequest.getEmail(), loginRequest.getPassword());
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
