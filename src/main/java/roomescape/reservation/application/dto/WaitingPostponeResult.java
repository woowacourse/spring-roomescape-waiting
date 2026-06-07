package roomescape.reservation.application.dto;

import roomescape.reservation.domain.Waiting;

public record WaitingPostponeResult(Long id, int rank) {

    public static WaitingPostponeResult from(Waiting waiting) {
        return new WaitingPostponeResult(
                waiting.getId(),
                waiting.getRank().value()
        );
    }
}
