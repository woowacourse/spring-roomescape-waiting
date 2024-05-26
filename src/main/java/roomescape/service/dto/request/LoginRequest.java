package roomescape.service.dto.request;

public record LoginRequest(String email, String password) {
    public LoginRequest {
        validate(email, password);
    }

    private void validate(String email, String password) {
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException();
        }
    }
}
