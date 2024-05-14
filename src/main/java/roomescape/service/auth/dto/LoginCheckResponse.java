package roomescape.service.auth.dto;

import roomescape.domain.member.Member;

public record LoginCheckResponse(String name) {
    public LoginCheckResponse(Member member) {
        this(member.getMemberName());
    }
}
