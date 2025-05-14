package roomescape.domain.member.controller.dto;

import roomescape.domain.member.application.dto.response.MemberServiceResponse;

public record MemberResponse(
        Long id,
        String name
) {

    public static MemberResponse from(MemberServiceResponse response) {
        return new MemberResponse(response.id(), response.name());
    }
}
