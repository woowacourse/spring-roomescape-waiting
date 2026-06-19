package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.reservation.domain.MemberName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;

public record ReservationApplicationCreateCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId,
        String paymentKey,
        Long amount,
        LocalDateTime now
) {
    public Reservation toReservation(ReservationSlot slot) {
        return Reservation.builder()
                .memberName(new MemberName(name))
                .slot(slot)
                .paymentKey(paymentKey)
                .amount(amount)
                .build();
    }

    public Waiting toWaiting(ReservationSlot slot) {
        return Waiting.builder()
                .memberName(new MemberName(name))
                .slot(slot)
                .build();
    }

    public ReservationSlot toSlot(LocalTime startAt) {
        return ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(startAt)
                .build();
    }
}
