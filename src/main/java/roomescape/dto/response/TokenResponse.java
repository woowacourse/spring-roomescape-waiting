package roomescape.dto.response;

public record TokenResponse(String accessToken) {
    public static TokenResponse from(String accessToken) {
        return new TokenResponse(accessToken);
    }
}
