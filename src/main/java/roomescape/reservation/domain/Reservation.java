package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import roomescape.global.exception.RoomEscapeException;
import roomescape.payment.PaymentStatus;

@Getter
public class Reservation {

    private final Long id;
    private final MemberName memberName;
    private final ReservationSlot slot;
    private final String paymentKey;
    private final Long amount;
    private final PaymentStatus paymentStatus;

    @Builder
    public Reservation(Long id, MemberName memberName, ReservationSlot slot, String paymentKey, Long amount, PaymentStatus paymentStatus) {
        this.id = id;
        this.memberName = memberName;
        this.slot = slot;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.paymentStatus = paymentStatus == null ? PaymentStatus.WAITING_FOR_DEPOSIT : paymentStatus;
    }

    public Reservation withId(Long generatedId) {
        return Reservation.builder()
                .id(generatedId)
                .memberName(this.memberName)
                .slot(this.slot)
                .paymentKey(this.paymentKey)
                .amount(this.amount)
                .paymentStatus(this.paymentStatus)
                .build();
    }

    public Reservation updateDateAndTime(LocalDate date, Long timeId, LocalTime startAt, LocalDateTime now) {
        ReservationSlot updatedSlot = slot.updateDateAndTime(date, timeId, startAt);
        updatedSlot.validateReservable(now);

        return Reservation.builder()
                .id(this.id)
                .memberName(this.memberName)
                .slot(updatedSlot)
                .paymentKey(this.paymentKey)
                .amount(this.amount)
                .paymentStatus(this.paymentStatus)
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
