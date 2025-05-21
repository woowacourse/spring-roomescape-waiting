package roomescape.application.auth.dto;

public record LoginCommand(
        String email,
        String password
) {
}
