package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.MemberOutput;

import java.util.List;

public record MembersResponse(List<MemberResponse> data) {
    public static MembersResponse from(final List<MemberOutput> outputs) {
        return new MembersResponse(
                outputs.stream()
                        .map(MemberResponse::from)
                        .toList());
    }
}
