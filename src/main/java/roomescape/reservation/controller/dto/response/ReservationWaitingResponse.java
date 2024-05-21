package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record ReservationWaitingResponse(
        int rank,
        String name,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "kk:mm")
        LocalTime time
) {
    public static ReservationWaitingResponse of(final Reservation reservation, final int rank) {
        return new ReservationWaitingResponse(
                rank,
                reservation.getMember().getNameValue(),
                reservation.getTheme().getThemeNameValue(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
