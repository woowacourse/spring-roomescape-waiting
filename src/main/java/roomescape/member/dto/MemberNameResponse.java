package roomescape.member.dto;

import roomescape.member.domain.LoginMember;

public record MemberNameResponse(String name) {

    public MemberNameResponse(LoginMember loginMember) {
        this(loginMember.getName().name());
    }
}
