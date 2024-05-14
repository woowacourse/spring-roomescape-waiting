package roomescape.auth.dto.response;

import roomescape.member.domain.Member;

public record GetAuthInfoResponse(String name) {
    public static GetAuthInfoResponse from(final Member member) {
        return new GetAuthInfoResponse(member.getName());
    }
}
