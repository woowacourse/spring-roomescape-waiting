package roomescape.member.presentation.dto;

import roomescape.member.domain.Member;

public class MemberResponse {
    private Long id;
    private String name;

    private MemberResponse() {
    }

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.name = member.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
