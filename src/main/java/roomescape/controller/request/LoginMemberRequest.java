package roomescape.controller.request;

import roomescape.service.param.LoginMemberParam;

public record LoginMemberRequest(String email, String password) {

    public LoginMemberParam toServiceParam() {
        return new LoginMemberParam(email, password);
    }
}
