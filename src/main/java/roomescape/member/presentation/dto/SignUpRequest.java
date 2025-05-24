package roomescape.member.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SignUpRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다")
    private final String email;

    @NotBlank(message = "비밀번호는 공백일 수 없습니다")
    private final String password;

    @NotBlank(message = "이름은 공백일 수 없습니다")
    private final String name;

    public SignUpRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
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
