package roomescape.domain.reservationslot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class ReservationSlot {

    private final Long id;
    private final LocalDate date;
    private final Theme theme;
    private final ReservationTime time;

    public ReservationSlot(final LocalDate date, final Theme theme, final ReservationTime time) {
        validate(date, theme, time);
        this.id = null;
        this.date = date;
        this.theme = theme;
        this.time = time;
    }

    public ReservationSlot(
            final Long id,
            final LocalDate date,
            final Theme theme,
            final ReservationTime time
    ) {
        validateId(id);
        validate(date, theme, time);
        this.id = id;
        this.date = date;
        this.theme = theme;
        this.time = time;
    }

    public boolean isPast(final LocalDateTime standardDateTime) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(standardDateTime);
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
