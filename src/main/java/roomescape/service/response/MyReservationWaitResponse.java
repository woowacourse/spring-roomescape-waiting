package roomescape.service.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.domain.reservation.ReservationWait;

public record MyReservationWaitResponse(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        Long rank
) {
    public static MyReservationWaitResponse from(final ReservationWait reservationWait) {
        return new MyReservationWaitResponse(
                reservationWait.getId(),
                reservationWait.getSchedule().getTheme().getName().name(),
                reservationWait.getSchedule().getDate(),
                reservationWait.getSchedule().getStartAt(),
                reservationWait.calculateRank()
        );
    }

    public static List<MyReservationWaitResponse> from(final List<ReservationWait> reservationWaits) {
        return reservationWaits.stream()
                .map(MyReservationWaitResponse::from)
                .toList();
    }
}
