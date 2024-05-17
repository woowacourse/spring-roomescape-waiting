package roomescape.core.dto.auth;

public class TokenResponse {
    private final String accessToken;

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
