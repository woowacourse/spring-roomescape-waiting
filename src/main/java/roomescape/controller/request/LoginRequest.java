package roomescape.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "이메일은 null 혹은 빈 문자열일 수 없습니다.")
    @Email(message = "이메일의 올바른 형식이어야 합니다.")
    private final String email;
    @NotBlank(message = "비밀번호는 null 혹은 빈 문자열일 수 없습니다.")
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
