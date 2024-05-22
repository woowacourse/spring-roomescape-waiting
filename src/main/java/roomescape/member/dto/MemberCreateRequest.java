package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberCreateRequest(String name, String email, String password) {
    public Member createMember() {
        return new Member(name, email, password);
    }
}
