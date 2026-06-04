package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class Reservation {
    public static final String PAST_RESERVATION_MESSAGE = "과거 날짜와 시간으로는 예약을 할 수 없습니다.";

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final Theme theme;
    private final ReservationTime time;
    private final LocalDateTime createdAt;

    private Reservation(
            final Long id,
            final String name,
            final LocalDate date,
            final Theme theme,
            final ReservationTime time,
            final LocalDateTime createdAt
    ) {
        ReservationName reservationName = ReservationName.from(name);
        validate(date, theme, time, createdAt);
        this.id = id;
        this.name = reservationName.value();
        this.date = date;
        this.theme = theme;
        this.time = time;
        this.createdAt = createdAt;
    }

    public static Reservation createNew(final String name, final LocalDate date, final Theme theme, final ReservationTime time) {
        return new Reservation(null, name, date, theme, time, LocalDateTime.now());
    }

    public static Reservation createNew(
            final String name,
            final LocalDate date,
            final Theme theme,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        validateReservable(date, time, standardDateTime);
        return new Reservation(null, name, date, theme, time, standardDateTime);
    }

    public static Reservation of(final Long id, final String name, final LocalDate date, final Theme theme, final ReservationTime time) {
        return of(id, name, date, theme, time, LocalDateTime.now());
    }

    public static Reservation of(
            final Long id,
            final String name,
            final LocalDate date,
            final Theme theme,
            final ReservationTime time,
            final LocalDateTime createdAt
    ) {
        validateId(id);
        return new Reservation(id, name, date, theme, time, createdAt);
    }

    public Reservation withId(final Long id) {
        validateId(id);
        return new Reservation(id, this.name, this.date, this.theme, this.time, this.createdAt);
    }

    public Reservation withDateAndTime(final LocalDate date, final ReservationTime time) {
        return new Reservation(this.id, this.name, date, this.theme, time, this.createdAt);
    }

    public Reservation withDateAndTime(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        validateReservable(date, time, standardDateTime);
        return new Reservation(this.id, this.name, date, this.theme, time, this.createdAt);
    }

    public boolean hasName(final String name) {
        return this.name.equals(ReservationName.from(name).value());
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

    private static void validateReservable(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        if (isPast(date, time, standardDateTime)) {
            throw new IllegalArgumentException(PAST_RESERVATION_MESSAGE);
        }
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id는 비어있을 수 없습니다.");
        }
    }

    private void validate(
            final LocalDate date,
            final Theme theme,
            final ReservationTime time,
            final LocalDateTime createdAt
    ) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 비어있을 수 없습니다.");
        }

        if (theme == null) {
            throw new IllegalArgumentException("테마는 비어있으면 안됩니다.");
        }

        if (time == null) {
            throw new IllegalArgumentException("시간은 비어있으면 안됩니다.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException("예약 생성 시각은 비어있으면 안됩니다.");
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Reservation)) {
            return false;
        }
        Reservation r = (Reservation) o;
        return Objects.equals(id, r.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public LocalDate getDate() {
        return this.slot.date();
    }

    public Theme getTheme() {
        return this.slot.theme();
    }

    public ReservationTime getTime() {
        return this.slot.time();
    }

    public ReservationSlot getSlot() {
        return this.slot;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
