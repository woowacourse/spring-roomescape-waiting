package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberResponse(Long id, String name) {

    public static MemberResponse toResponse(Member member) {
        return new MemberResponse(member.getId(), member.getName());
    }
}
