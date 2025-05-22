package roomescape.application.member.dto;

public record RegisterParam(
        String email,
        String password,
        String name
) {
}
