package roomescape.dto.login;

public record LoginRequest(
        String email,
        String password
) {
}
