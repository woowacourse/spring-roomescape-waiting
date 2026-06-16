package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
public class Reservation {

    @With
    private final Long id;
    private final User user;
    private final ReservationSlot slot;
    private final ReservationStatus status;

    @Builder
    public Reservation(Long id, User user, ReservationSlot slot, ReservationStatus status) {
        this.id = id;
        this.user = Objects.requireNonNull(user);
        this.slot = Objects.requireNonNull(slot);
        this.status = Objects.requireNonNull(status);
    }

    public static Reservation create(User user, ReservationSlot slot, LocalDateTime now) {
        slot.validateReservable(now);

        return Reservation.builder()
                .user(user)
                .slot(slot)
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();
    }

    public Reservation updateDateAndTime(LocalDate date, Long timeId, LocalTime startAt, LocalDateTime now) {
        slot.validateUpdatable(now);
        ReservationSlot updatedSlot = slot.updateDateAndTime(date, timeId, startAt);
        updatedSlot.validateReservable(now);

        return Reservation.builder()
                .id(this.id)
                .user(this.user)
                .slot(updatedSlot)
                .status(this.status)
                .build();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Reservation reservation)) {
            return false;
        }
        return id != null && id.equals(reservation.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public String getUserName() {
        return user.name();
    }
}
