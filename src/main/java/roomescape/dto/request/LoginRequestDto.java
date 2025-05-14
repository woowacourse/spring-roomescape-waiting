package roomescape.dto.request;

public record LoginRequestDto(
        String email,
        String password
) {
}
