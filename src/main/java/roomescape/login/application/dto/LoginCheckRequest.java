package roomescape.login.application.dto;

public record LoginCheckRequest(
        Long id
) {
    public static LoginCheckRequest from(final Long id) {
        return new LoginCheckRequest(id);
    }
}
