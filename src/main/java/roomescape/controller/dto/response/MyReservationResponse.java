package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Reservation;
import roomescape.entity.Waiting;
import roomescape.entity.WaitingWithRank;

public record MyReservationResponse(
    Long reservationId,
    String theme,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    @JsonFormat(pattern = "HH:mm") LocalTime time,
    String status
) {
    private static final int ADDITIONAL_RANK = 1;
    private static final String RANK_SUFFIX = "번째 ";

    public static MyReservationResponse fromReservation(Reservation reservation) {
        return new MyReservationResponse(
            reservation.getId(),
            reservation.getThemeName(),
            reservation.getDate(),
            reservation.getStartAt(),
            reservation.getStatusText()
        );
    }

    public static MyReservationResponse fromWaiting(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getThemeName(),
                waiting.getDate(),
                waiting.getStartAt(),
                waitingWithRank.getRank()+ ADDITIONAL_RANK + RANK_SUFFIX + waiting.getStatusText()
        );
    }
}
