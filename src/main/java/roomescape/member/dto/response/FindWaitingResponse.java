package roomescape.member.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Waiting;

public record FindWaitingResponse(Long waitingId,
                                  String theme,
                                  LocalDate date,
                                  LocalTime time,
                                  Long waitingNumber) {
    public static FindWaitingResponse from(final Waiting waiting) {
        return new FindWaitingResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getReservationTime().getStartAt(),
                1L);
    }
}
