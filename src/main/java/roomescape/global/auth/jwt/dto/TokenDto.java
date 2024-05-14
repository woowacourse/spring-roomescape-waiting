package roomescape.global.auth.jwt.dto;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
