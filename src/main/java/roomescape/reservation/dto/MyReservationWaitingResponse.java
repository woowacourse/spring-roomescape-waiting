package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.WaitingWithOrder;

public record MyReservationWaitingResponse(
        String themeName,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        String status) {
    private static final String RESERVATION_STATUS = "예약";
    private static final String WAITING_STATUS = "%d번째 예약대기";

    public static MyReservationWaitingResponse from(Reservation reservation) {
        return new MyReservationWaitingResponse(
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                RESERVATION_STATUS);
    }

    public static MyReservationWaitingResponse from(WaitingWithOrder waitingWithOrder) {
        return new MyReservationWaitingResponse(
                waitingWithOrder.getWaiting().getReservation().getTheme().getName(),
                waitingWithOrder.getWaiting().getReservation().getDate(),
                waitingWithOrder.getWaiting().getReservation().getTime().getStartAt(),
                String.format(WAITING_STATUS, waitingWithOrder.getOrder()));
    }
}
