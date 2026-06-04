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
    private final ReservationSlot slot;
    private final LocalDateTime createdAt;

    private Reservation(
            final Long id,
            final String name,
            final ReservationSlot slot,
            final LocalDateTime createdAt
    ) {
        ReservationName reservationName = ReservationName.from(name);
        validate(createdAt);
        this.id = id;
        this.name = reservationName.value();
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public static Reservation createNew(final String name, final LocalDate date, final Theme theme, final ReservationTime time) {
        return createNew(name, new ReservationSlot(date, theme, time), LocalDateTime.now());
    }

    public static Reservation createNew(
            final String name,
            final LocalDate date,
            final Theme theme,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        return createNew(name, new ReservationSlot(date, theme, time), standardDateTime);
    }

    public static Reservation createNew(
            final String name,
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        validateReservable(slot, standardDateTime);
        return new Reservation(null, name, slot, standardDateTime);
    }

    public static Reservation of(final Long id, final String name, final LocalDate date, final Theme theme, final ReservationTime time) {
        return of(id, name, new ReservationSlot(date, theme, time), LocalDateTime.now());
    }

    public static Reservation of(
            final Long id,
            final String name,
            final LocalDate date,
            final Theme theme,
            final ReservationTime time,
            final LocalDateTime createdAt
    ) {
        return of(id, name, new ReservationSlot(date, theme, time), createdAt);
    }

    public static Reservation of(
            final Long id,
            final String name,
            final ReservationSlot slot,
            final LocalDateTime createdAt
    ) {
        validateId(id);
        return new Reservation(id, name, slot, createdAt);
    }

    public Reservation withId(final Long id) {
        validateId(id);
        return new Reservation(id, this.name, this.slot, this.createdAt);
    }

    public Reservation withDateAndTime(final LocalDate date, final ReservationTime time) {
        return new Reservation(this.id, this.name, new ReservationSlot(date, this.slot.theme(), time), this.createdAt);
    }

    public Reservation withDateAndTime(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        ReservationSlot changedSlot = new ReservationSlot(date, this.slot.theme(), time);
        validateReservable(changedSlot, standardDateTime);
        return new Reservation(this.id, this.name, changedSlot, this.createdAt);
    }

    public boolean hasName(final String name) {
        return this.name.equals(ReservationName.from(name).value());
    }

    public boolean isPast(final LocalDateTime standardDateTime) {
        return slot.isPast(standardDateTime);
    }

    public static boolean isReservable(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        return ReservationSlot.isReservable(date, time, standardDateTime);
    }

    public static boolean isPast(
            final LocalDate date,
            final ReservationTime time,
            final LocalDateTime standardDateTime
    ) {
        return ReservationSlot.isPast(date, time, standardDateTime);
    }

    public static boolean isPast(
            final LocalDate date,
            final LocalTime startAt,
            final LocalDateTime standardDateTime
    ) {
        return ReservationSlot.isPast(date, startAt, standardDateTime);
    }

    private static void validateReservable(
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        if (slot.isPast(standardDateTime)) {
            throw new IllegalArgumentException(PAST_RESERVATION_MESSAGE);
        }
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id는 비어있을 수 없습니다.");
        }
    }

    private void validate(final LocalDateTime createdAt) {
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
}
