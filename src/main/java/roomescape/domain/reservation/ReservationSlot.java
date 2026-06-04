package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public record ReservationSlot(
        LocalDate date,
        Theme theme,
        ReservationTime time
) {

    public ReservationSlot {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 비어있을 수 없습니다.");
        }

        if (theme == null) {
            throw new IllegalArgumentException("테마는 비어있으면 안됩니다.");
        }

        if (time == null) {
            throw new IllegalArgumentException("시간은 비어있으면 안됩니다.");
        }
    }

    public boolean isPast(final LocalDateTime standardDateTime) {
        return isPast(date, time, standardDateTime);
    }

    public static boolean isReservable(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        return !isPast(date, time, standardDateTime);
    }

    public static boolean isPast(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        return isPast(date, time.getStartAt(), standardDateTime);
    }

    public static boolean isPast(
            final LocalDate date,
            final LocalTime startAt,
            final LocalDateTime standardDateTime
    ) {
        return LocalDateTime.of(date, startAt).isBefore(standardDateTime);
    }
}
