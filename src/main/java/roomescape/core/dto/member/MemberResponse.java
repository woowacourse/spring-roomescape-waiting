package roomescape.core.dto.member;

import roomescape.core.domain.Member;

public class MemberResponse {
    private Long id;
    private String name;

    public MemberResponse() {
    }

    public MemberResponse(final Member member) {
        this(member.getId(), member.getName());
    }

    public MemberResponse(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
