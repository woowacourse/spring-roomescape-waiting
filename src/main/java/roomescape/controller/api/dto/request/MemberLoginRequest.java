package roomescape.controller.api.dto.request;

import roomescape.service.dto.input.MemberLoginInput;

public record MemberLoginRequest (String email, String password){
    public MemberLoginInput toInput() {
        return new MemberLoginInput(email,password);
    }
}
