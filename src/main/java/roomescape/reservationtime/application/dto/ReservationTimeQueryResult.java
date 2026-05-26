package roomescape.reservationtime.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeQueryResult(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static ReservationTimeQueryResult from(ReservationTime time) {
        return new ReservationTimeQueryResult(
                time.getId(),
                time.getStartAt()
        );
    }

    public static ReservationTimeQueryResult from(Long id, LocalTime startAt) {
        return new ReservationTimeQueryResult(
                id,
                startAt
        );
    }
}
