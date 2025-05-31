package roomescape.dto.member;

public record MemberSignupResponseDto(
        Long id,
        String name,
        String email
) {
}
