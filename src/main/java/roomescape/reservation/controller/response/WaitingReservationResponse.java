package roomescape.reservation.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.waiting.domain.Waiting;

public record WaitingReservationResponse(
        Long waitingId,
        String reserverName,
        String themeName,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time
) {
    public static WaitingReservationResponse from(Waiting waiting) {
        return new WaitingReservationResponse(
                waiting.getId(),
                waiting.getWaiter().getName(),
                waiting.getTheme().getName(),
                waiting.getReservationDatetime().reservationDate().date(),
                waiting.getReservationDatetime().reservationTime().getStartAt()
        );
    }

    public static List<WaitingReservationResponse> from(List<Waiting> waitings) {
        return waitings.stream()
                .map(WaitingReservationResponse::from)
                .toList();
    }
}
