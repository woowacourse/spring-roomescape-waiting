package roomescape.controller.response;

import roomescape.model.member.MemberWithoutPassword;

public class LoginResponse {

    private String name;

    private LoginResponse(String name) {
        this.name = name;
    }

    public static LoginResponse from(MemberWithoutPassword loginMember) {
        return new LoginResponse(loginMember.getName());
    }

    private LoginResponse() {
    }

    public String getName() {
        return name;
    }
}
