package roomescape.member.application.dto;

import roomescape.member.domain.Member;

public record MemberInfo(Long id, String name, String email, String password) {

    public MemberInfo(Member member) {
        this(member.id(), member.memberName().name(), member.email(), member.password());
    }
}
