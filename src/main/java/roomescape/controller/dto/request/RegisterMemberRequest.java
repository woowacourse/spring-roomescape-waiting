package roomescape.controller.dto.request;

import roomescape.service.dto.param.RegisterMemberParam;

public record RegisterMemberRequest(String email, String password, String name) {
    public RegisterMemberParam toServiceParam() {
        return new RegisterMemberParam(email, password, name);
    }
}
