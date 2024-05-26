package roomescape.service.dto.response;

import static roomescape.domain.ReservationStatus.BOOKED;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Waiting;

public record ReservationMineResponse(
        Long id,
        ThemeResponse theme,
        LocalDate date,
        ReservationTimeResponse time,
        ReservationStatus status,
        Long rank
) {
    public ReservationMineResponse(Reservation reservation) {
        this(reservation.getId(),
                new ThemeResponse(reservation.getTheme()),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getTime()),
                BOOKED,
                0L
        );
    }

    public ReservationMineResponse(Waiting waiting, Long rank) {
        this(waiting.getId(),
                new ThemeResponse(waiting.getTheme()),
                waiting.getDate(),
                new ReservationTimeResponse(waiting.getTime()),
                waiting.getStatus(),
                rank
        );
    }

    public String getStatus() {
        return status.getDescription();
    }
}
