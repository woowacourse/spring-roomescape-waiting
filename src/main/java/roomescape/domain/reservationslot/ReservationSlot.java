package roomescape.domain.reservationslot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class ReservationSlot {

    private final Long id;
    private final LocalDate date;
    private final Theme theme;
    private final ReservationTime time;

    private ReservationSlot(final Long id, final LocalDate date, final Theme theme, final ReservationTime time) {
        validate(date, theme, time);
        this.id = id;
        this.date = date;
        this.theme = theme;
        this.time = time;
    }

    public static ReservationSlot createNew(final LocalDate date, final Theme theme, final ReservationTime time) {
        return new ReservationSlot(null, date, theme, time);
    }

    public static ReservationSlot of(final Long id, final LocalDate date, final Theme theme, final ReservationTime time) {
        validateId(id);
        return new ReservationSlot(id, date, theme, time);
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

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id는 비어있을 수 없습니다.");
        }
    }

    private static void validate(final LocalDate date, final Theme theme, final ReservationTime time) {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 비어있으면 안됩니다.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("테마는 비어있으면 안됩니다.");
        }
        if (time == null) {
            throw new IllegalArgumentException("예약 시간은 비어있으면 안됩니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return time;
    }
}
