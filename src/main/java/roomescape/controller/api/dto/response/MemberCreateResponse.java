package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.MemberCreateOutput;

public record MemberCreateResponse(long id, String email, String name, String password) {
    public static MemberCreateResponse toResponse(final MemberCreateOutput output) {
        return new MemberCreateResponse(output.id(), output.email(), output.name(), output.password());
    }
}
