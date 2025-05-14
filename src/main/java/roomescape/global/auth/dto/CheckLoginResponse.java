package roomescape.global.auth.dto;

public record CheckLoginResponse(String name) {

    public static CheckLoginResponse from(final UserInfo userInfo) {
        return new CheckLoginResponse(userInfo.name());
    }
}
