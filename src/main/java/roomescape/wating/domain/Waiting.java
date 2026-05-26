package roomescape.wating.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Waiting {

    private final Long id;
    private final CustomerName customerName;
    private final LocalDate reservationDate;
    private final LocalDateTime createdAt;
    private final ReservationTime time;
    private final Theme theme;

    private Waiting(
            Long id,
            CustomerName customerName,
            LocalDate reservationDate,
            LocalDateTime createdAt,
            ReservationTime time,
            Theme theme
    ) {
        this.id = id;
        this.customerName = customerName;
        this.reservationDate = reservationDate;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
    }
}
