package roomescape.service.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import roomescape.domain.Member;

public record LoginCheckResponse(String name) {
    public LoginCheckResponse(Member member) {
        this(member.getName());
    }
}
