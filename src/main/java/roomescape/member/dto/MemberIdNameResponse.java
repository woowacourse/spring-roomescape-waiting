package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberIdNameResponse(Long id, String name) {

    public MemberIdNameResponse(Member member) {
        this(member.getId(), member.getName().name());
    }
}
