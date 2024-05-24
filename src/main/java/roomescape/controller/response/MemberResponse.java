package roomescape.controller.response;

import roomescape.model.member.Member;

public class MemberResponse {

    private final long id;
    private final String name;

    private MemberResponse(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
