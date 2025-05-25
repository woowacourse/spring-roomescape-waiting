package roomescape.waiting.presentation.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record WaitingResponse(Long waitingId, String name, String themeName, String date, String startAt) {

    public static WaitingResponse from(final Waiting waiting) {
        Reservation reservation = waiting.getReservation();
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate().toString(),
                reservation.getTime().getStartAt().toString()
        );
    }
}
