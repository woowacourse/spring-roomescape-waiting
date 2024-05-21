package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import roomescape.domain.Member;

public class LoginCheckResponse {
    private final String name;

    @JsonCreator
    public LoginCheckResponse(String name) {
        this.name = name;
    }

    public LoginCheckResponse(Member member) {
        this(member.getName());
    }

    public String getName() {
        return name;
    }
}
