package roomescape.member.dto;

import roomescape.exception.custom.reason.ResponseInvalidException;
import roomescape.member.Member;

public record MemberResponse(Long id, String name) {

    public MemberResponse {
        if (id == null || name == null) {
            throw new ResponseInvalidException();
        }
    }

    public static MemberResponse from(final Member member) {
        return new MemberResponse(member.getId(), member.getName());
    }
}
