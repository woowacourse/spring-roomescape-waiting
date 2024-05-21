package roomescape.service.dto;

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
            throw new IllegalArgumentException();
        }
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
