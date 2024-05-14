package roomescape.domain.dto;

import roomescape.domain.Member;

public record LoginResponse(String name) {
    public static LoginResponse from(final Member member) {
        return new LoginResponse(member.getName());
    }
}
