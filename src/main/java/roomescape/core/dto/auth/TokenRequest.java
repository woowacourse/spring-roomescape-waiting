package roomescape.core.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class TokenRequest {
    @Email
    private String email;
    @NotBlank
    private String password;

    public TokenRequest() {
    }

    public TokenRequest(final String email, final String password) {
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
