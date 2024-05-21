package roomescape.service.dto;

import roomescape.domain.Member;

public class MemberResponse {
    private final Long id;
    private final String name;
    private final String email;

    public MemberResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public MemberResponse(Member member) {
        this(member.getId(), member.getName(), member.getEmail());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
