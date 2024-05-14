package roomescape.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
