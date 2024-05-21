package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record MyReservationResponse(Long id,
                                    String themeName,
                                    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                    @JsonFormat(pattern = "HH:mm") LocalTime startAt,
                                    String status,
                                    Long waitingId) {
    private static final String RESERVATION_STATUS = "예약";
    private static final String WAITING_STATUS_FORMAT = "%d번째 예약 대기";

    public static MyReservationResponse from(Reservation reservation) {
        return createResponse(reservation, RESERVATION_STATUS, null);
    }

    public static MyReservationResponse from(Waiting waiting, Long order) {
        String status = WAITING_STATUS_FORMAT.formatted(order);
        return createResponse(waiting.getReservation(), status, waiting.getId());
    }

    private static MyReservationResponse createResponse(Reservation reservation, String status, Long waitingId) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                status,
                waitingId);
    }
}
