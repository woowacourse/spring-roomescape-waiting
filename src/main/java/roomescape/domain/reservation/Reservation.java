package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class Reservation {
    public static final String PAST_RESERVATION_MESSAGE = "과거 날짜와 시간으로는 예약을 할 수 없습니다.";

    private final Long id;
    private final ReservationName name;
    private final ReservationSlot slot;
    private final LocalDateTime createdAt;

    public Reservation(
            final Long id,
            final String name,
            final ReservationSlot slot,
            final LocalDateTime createdAt
    ) {
        validateId(id);
        validateSlot(slot);
        validate(createdAt);
        this.id = id;
        this.name = ReservationName.from(name);
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public Reservation(
            final String name,
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        validateReservable(slot, standardDateTime);
        validate(standardDateTime);
        this.id = null;
        this.name = ReservationName.from(name);
        this.slot = slot;
        this.createdAt = standardDateTime;
    }

    public Reservation withId(final Long id) {
        validateId(id);
        return new Reservation(id, this.name.value(), this.slot, this.createdAt);
    }

    public Reservation withSlot(
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        validateReservable(slot, standardDateTime);
        return new Reservation(this.id, this.name.value(), slot, this.createdAt);
    }

    public boolean hasName(final String name) {
        return this.name.value().equals(ReservationName.from(name).value());
    }

    public boolean isPast(final LocalDateTime standardDateTime) {
        return slot.isPast(standardDateTime);
    }

    private static void validateReservable(
            final ReservationSlot slot,
            final LocalDateTime standardDateTime
    ) {
        validateSlot(slot);
        if (slot.isPast(standardDateTime)) {
            throw new IllegalArgumentException(PAST_RESERVATION_MESSAGE);
        }
    }

    private static void validateSlot(final ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯은 비어있으면 안됩니다.");
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
        return this.name.value();
    }

    public LocalDate getDate() {
        return this.slot.getDate();
    }

    public Theme getTheme() {
        return this.slot.getTheme();
    }

    public ReservationTime getTime() {
        return this.slot.getTime();
    }

    public ReservationSlot getSlot() {
        return this.slot;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
