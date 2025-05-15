package roomescape.presentation.dto;

public record LoginRequest(String email, String password) {
    public LoginRequest {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일이 비어있습니다.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호가 비어있습니다.");
        }
    }
}
