package roomescape.service.dto.response;

import roomescape.domain.Member;

public record LoginCheckResponse(String name) {
    public LoginCheckResponse(Member member) {
        this(member.getName());
    }
}
