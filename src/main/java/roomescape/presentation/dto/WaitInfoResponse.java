package roomescape.presentation.dto;

import roomescape.business.domain.WaitInfo;

public record WaitInfoResponse(Long id, MemberResponse memberResponse) {

    public static WaitInfoResponse from(final WaitInfo waitInfo) {
        return new WaitInfoResponse(waitInfo.getId(), MemberResponse.from(waitInfo.getMember()));
    }
}
