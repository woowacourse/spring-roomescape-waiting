package roomescape.login.presentation.request;

public record LoginCheckRequest(
        Long id
) {
    public static LoginCheckRequest from(final Long id) {
        return new LoginCheckRequest(id);
    }
}
