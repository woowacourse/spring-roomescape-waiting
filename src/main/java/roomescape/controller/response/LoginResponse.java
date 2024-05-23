package roomescape.controller.response;

import roomescape.service.dto.MemberInfo;

public class LoginResponse {

    private String name;

    private LoginResponse(String name) {
        this.name = name;
    }

    public static LoginResponse from(MemberInfo memberInfo) {
        return new LoginResponse(memberInfo.getName());
    }

    private LoginResponse() {
    }

    public String getName() {
        return name;
    }
}
