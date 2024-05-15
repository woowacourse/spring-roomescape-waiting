package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberNameResponse(String name) {

    public MemberNameResponse(Member member) {
        this(member.getName().name());
    }
}
