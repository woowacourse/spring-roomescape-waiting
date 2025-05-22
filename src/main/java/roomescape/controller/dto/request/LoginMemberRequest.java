package roomescape.controller.dto.request;

import roomescape.service.dto.param.LoginMemberParam;

public record LoginMemberRequest(String email, String password) {

    public LoginMemberParam toServiceParam() {
        return new LoginMemberParam(email, password);
    }
}
