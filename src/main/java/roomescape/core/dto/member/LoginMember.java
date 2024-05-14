package roomescape.core.dto.member;

import roomescape.core.domain.Member;

public class LoginMember {
    private Long id;

    public LoginMember() {
    }

    public LoginMember(final Member member) {
        this(member.getId());
    }

    public LoginMember(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
