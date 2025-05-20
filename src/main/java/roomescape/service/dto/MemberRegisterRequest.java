package roomescape.service.dto;

public record MemberRegisterRequest(
        String email,
        String password,
        String name
) {
}
