package roomescape.controller.response;

import roomescape.model.member.Member;

public class MemberResponse {

    private final long id;
    private final String name;
    private final String email;

    private MemberResponse(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
