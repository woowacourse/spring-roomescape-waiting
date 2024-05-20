package roomescape.controller.api.dto.request;

import roomescape.service.dto.input.MemberCreateInput;

public record MemberCreateRequest(String email, String password, String name) {
    public MemberCreateInput toInput() {
        return new MemberCreateInput(name, email, password);
    }
}
