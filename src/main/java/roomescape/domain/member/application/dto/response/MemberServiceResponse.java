package roomescape.domain.member.application.dto.response;

import roomescape.domain.member.model.Member;

public record MemberServiceResponse(
        Long id,
        String name
) {

    public static MemberServiceResponse from(Member member) {
        return new MemberServiceResponse(member.getId(), member.getName());
    }
}
