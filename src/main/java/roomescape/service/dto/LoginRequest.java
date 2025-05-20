package roomescape.service.dto;

public record LoginRequest(
        String email,
        String password
) {
}
