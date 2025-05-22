package roomescape.reservation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingWithRank;

public record MyReservation(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    static final String RESERVED_STATUS = "예약";
    static final String WAITING_STATUS = "%s번째 예약대기";

    public static MyReservation from(Reservation reservation) {
        return new MyReservation(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
            RESERVED_STATUS
        );
    }

    public static MyReservation from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        return new MyReservation(
            waiting.getId(),
            waiting.getTheme().getName(),
            waiting.getDate(),
            waiting.getReservationTime().getStartAt(),
            WAITING_STATUS.formatted(waitingWithRank.getRank())
        );
    }
}
