package roomescape.domain.reservationwaiting;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservationslot.ReservationSlot;

public class ReservationWaiting {
    public static final String PAST_WAITING_MESSAGE = "지난 예약에는 대기를 생성할 수 없습니다.";

    private final Long id;
    private final ReservationSlot slot;
    private final ReservationName name;
    private final LocalDateTime requestedAt;

    private ReservationWaiting(
            final Long id,
            final ReservationSlot slot,
            final String name,
            final LocalDateTime requestedAt
    ) {
        this.id = id;
        this.slot = slot;
        this.name = ReservationName.from(name);
        this.requestedAt = requestedAt;
        validateWaitable();
    }

    public static ReservationWaiting createNew(
            final ReservationSlot slot,
            final String name,
            final LocalDateTime requestedAt
    ) {
        return new ReservationWaiting(null, slot, name, requestedAt);
    }

    public static ReservationWaiting of(
            final Long id,
            final ReservationSlot slot,
            final String name,
            final LocalDateTime requestedAt
    ) {
        return new ReservationWaiting(id, slot, name, requestedAt);
    }

    public ReservationWaiting withId(final Long id) {
        return ReservationWaiting.of(id, this.slot, this.name.value(), this.requestedAt);
    }

    public Long getId() {
        return id;
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    public String getName() {
        return name.value();
    }

    public boolean hasName(final String name) {
        return this.name.value().equals(ReservationName.from(name).value());
    }

    public Reservation toReservation(final LocalDateTime requestedAt) {
        return Reservation.reserve(name.value(), slot, requestedAt);
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    private void validateWaitable() {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯은 비어있으면 안됩니다.");
        }
        if (slot.isPast(requestedAt)) {
            throw new IllegalArgumentException(PAST_WAITING_MESSAGE);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ReservationWaiting)) {
            return false;
        }

        ReservationWaiting reservationWaiting = (ReservationWaiting) o;
        return id != null && Objects.equals(id, reservationWaiting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
