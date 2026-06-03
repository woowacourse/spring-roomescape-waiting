package roomescape.reservation.presentation.dto;

import roomescape.reservation.application.dto.WaitingPostponeResult;

public record WaitingPostponeResponse(Long id, int rank) {

    public static WaitingPostponeResponse from(WaitingPostponeResult result) {
        return new WaitingPostponeResponse(
                result.id(),
                result.rank()
        );
    }
}
