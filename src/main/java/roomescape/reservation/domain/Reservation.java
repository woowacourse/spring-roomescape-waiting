package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import roomescape.global.exception.RoomEscapeException;

@Getter
public class Reservation {

    private final Long id;
    private final MemberName memberName;
    private final ReservationSlot slot;

    @Builder
    public Reservation(Long id, MemberName memberName, ReservationSlot slot) {
        this.id = id;
        this.memberName = memberName;
        this.slot = slot;
    }

    public Reservation withId(Long generatedId) {
        return Reservation.builder()
                .id(generatedId)
                .memberName(this.memberName)
                .slot(this.slot)
                .build();
    }

    public Reservation updateDateAndTime(LocalDate date, Long timeId, LocalTime startAt, LocalDateTime now) {
        ReservationSlot updatedSlot = slot.updateDateAndTime(date, timeId, startAt);
        updatedSlot.validateReservable(now);

        return Reservation.builder()
                .id(this.id)
                .memberName(this.memberName)
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
}
