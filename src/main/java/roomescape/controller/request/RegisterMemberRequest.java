package roomescape.controller.request;

import roomescape.service.param.RegisterMemberParam;

public record RegisterMemberRequest(String email, String password, String name) {
    public RegisterMemberParam toServiceParam() {
        return new RegisterMemberParam(email, password, name);
    }
}
