package roomescape.service.login.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import roomescape.domain.member.Member;

public class LoginCheckResponse {
    private final String name;

    @JsonCreator
    public LoginCheckResponse(String name) {
        this.name = name;
    }

    public LoginCheckResponse(Member member) {
        this(member.getName().getName());
    }

    public String getName() {
        return name;
    }
}
