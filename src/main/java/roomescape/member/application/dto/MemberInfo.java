package roomescape.member.application.dto;

import roomescape.member.domain.Member;

public record MemberInfo(Long id, String name, String email, String password) {

    public MemberInfo(final Member member) {
        this(member.id(), member.getNameOfMember(), member.email(), member.password());
    }
}
