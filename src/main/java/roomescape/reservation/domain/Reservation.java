package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Reservation {

    private final Long id;
    private final User user;
    private final ReservationSlot slot;

    @Builder
    public Reservation(Long id, User user, ReservationSlot slot) {
        this.id = id;
        this.user = Objects.requireNonNull(user);
        this.slot = Objects.requireNonNull(slot);
    }

    public static Reservation create(User user, ReservationSlot slot, LocalDateTime now) {
        slot.validateReservable(now);

        return Reservation.builder()
                .user(user)
                .slot(slot)
                .build();
    }

    public Reservation withId(Long generatedId) {
        return Reservation.builder()
                .id(generatedId)
                .user(this.user)
                .slot(this.slot)
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
