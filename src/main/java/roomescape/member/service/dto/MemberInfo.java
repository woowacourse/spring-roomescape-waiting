package roomescape.member.service.dto;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record MemberInfo(long id, String name, String email, MemberRole role) {

    public MemberInfo(Member member) {
        this(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }
}
