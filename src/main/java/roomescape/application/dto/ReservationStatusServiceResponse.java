package roomescape.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import org.springframework.context.MessageSource;
import roomescape.domain.WaitingOrder;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Reservation;
import roomescape.domain.entity.Waiting;

public record ReservationStatusServiceResponse(
        long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static ReservationStatusServiceResponse of(Reservation reservation, MessageSource messageSource) {
        GameSchedule gameSchedule = reservation.getGameSchedule();
        return new ReservationStatusServiceResponse(
                reservation.getId(),
                gameSchedule.getTheme().getName(),
                gameSchedule.getDate(),
                gameSchedule.getTime().getStartAt(),
                messageSource.getMessage(reservation.getStatus().name(), null, Locale.KOREA)
        );
    }

    public static ReservationStatusServiceResponse of(Waiting waiting, WaitingOrder order, MessageSource messageSource) {
        GameSchedule gameSchedule = waiting.getGameSchedule();
        return new ReservationStatusServiceResponse(
                waiting.getId(),
                gameSchedule.getTheme().getName(),
                gameSchedule.getDate(),
                gameSchedule.getTime().getStartAt(),
                messageSource.getMessage(waiting.getStatus().name(), new Object[]{order.value() + "번째 "}, Locale.KOREA)
        );
    }
}
