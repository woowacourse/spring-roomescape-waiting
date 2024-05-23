package roomescape.service.auth.dto;

public record AuthenticationInfoResponse(String name) {

    public static AuthenticationInfoResponse from(String name) {
        return new AuthenticationInfoResponse(name);
    }
}
