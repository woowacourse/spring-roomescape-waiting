package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.MemberOutput;

public record MemberResponse (long id, String name){
    public static MemberResponse toResponse(final MemberOutput output) {
        return new MemberResponse(
                output.id(),
                output.name()
        );
    }
}
