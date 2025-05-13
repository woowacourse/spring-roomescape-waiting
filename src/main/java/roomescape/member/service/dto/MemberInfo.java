package roomescape.member.service.dto;

import roomescape.member.domain.Member;

public record MemberInfo(long id, String name, String email, String password) {

    public MemberInfo(Member member) {
        this(member.getId(), member.getName(), member.getEmail(), member.getPassword());
    }
}
