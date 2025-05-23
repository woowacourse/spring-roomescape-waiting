package roomescape.member.controller.dto.response;


import roomescape.member.entity.Member;

public record MemberResponse(
    Long id,
    String name,
    String email
) {
    public static MemberResponse from(final Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }
}
