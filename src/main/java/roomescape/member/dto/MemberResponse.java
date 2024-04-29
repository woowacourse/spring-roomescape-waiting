package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberResponse(Long id, String name) {
    public MemberResponse(Member member) {
        this(member.getId(), member.getName());
    }
}
