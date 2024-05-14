package roomescape.member.dto;

import roomescape.member.domain.LoginMember;

public record MemberIdNameResponse(Long id, String name) {

    public MemberIdNameResponse(LoginMember loginMember) {
        this(loginMember.getId(), loginMember.getName().name());
    }
}
