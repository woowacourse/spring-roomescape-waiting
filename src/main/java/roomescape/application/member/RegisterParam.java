package roomescape.application.member;

public record RegisterParam(
        String email,
        String password,
        String name
) {
}
