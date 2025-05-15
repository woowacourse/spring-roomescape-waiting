package roomescape.dto;

public record MemberRegisterRequest(
        String email,
        String password,
        String name
) {
}
