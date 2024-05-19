package roomescape.controller.member.dto;

import roomescape.domain.Member;

public record MemberLoginResponse(Long id, String name) {

    public static MemberLoginResponse from(final Member member) {
        return new MemberLoginResponse(member.getId(), member.getName());
    }
}
