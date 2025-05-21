package roomescape.dto.request;

public record MemberRegisterRequest(
        String email,
        String password,
        String name
) {
}
