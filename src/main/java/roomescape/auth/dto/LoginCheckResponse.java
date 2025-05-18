package roomescape.auth.dto;

import roomescape.member.domain.Member;

public record LoginCheckResponse(String name) {

    public LoginCheckResponse(final Member member) {
        this(member.getName());
    }
}
