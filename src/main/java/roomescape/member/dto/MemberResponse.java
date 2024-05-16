package roomescape.member.dto;

public record MemberResponse(Long id, String name, String email) {
    public static MemberResponse from(final MemberDto member) {
        return new MemberResponse(
                member.id(),
                member.name().getValue(),
                member.email().getValue()
        );
    }
}
