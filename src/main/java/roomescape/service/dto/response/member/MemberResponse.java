package roomescape.service.dto.response.member;

import roomescape.domain.Member;

public record MemberResponse(Long id, String name) {

    public MemberResponse(String name) {
        this(null, name);
    }

    public MemberResponse(Member member) {
        this(member.getId(), member.getName());
    }
}
