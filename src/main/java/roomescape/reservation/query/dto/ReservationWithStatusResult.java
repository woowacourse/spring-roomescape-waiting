package roomescape.reservation.query.dto;

import java.time.LocalDate;
import java.util.Comparator;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.ReservationWaiting;

public record ReservationWithStatusResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        String status,
        Long waitingOrder
) implements Comparable<ReservationWithStatusResult> {

    @Override
    public int compareTo(ReservationWithStatusResult other) {
        return Comparator.comparing(ReservationWithStatusResult::status, (status1, status2) -> {
                    if (status1.equals(status2)) {
                        return 0;
                    }
                    if ("reserved".equals(status1)) {
                        return -1;
                    }
                    return 1;
                })
                .thenComparing(ReservationWithStatusResult::date)
                .thenComparing(result -> result.time().getStartAt())
                .thenComparing(ReservationWithStatusResult::waitingOrder)
                .compare(this, other);
    }

    public static ReservationWithStatusResult from(Reservation reservation) {
        return new ReservationWithStatusResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                "reserved",
                0L
        );
    }

    public static ReservationWithStatusResult from(ReservationWaiting waiting, long rank) {
        return new ReservationWithStatusResult(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                "waiting",
                rank
        );
    }
}
