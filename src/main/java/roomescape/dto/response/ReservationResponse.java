package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.WaitingWithRank;
import roomescape.payment.PaymentDetails;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        TimeInfo time,
        ThemeInfo theme,

        ReservationStatus status,
        Integer rank,
        PaymentInfoResponse payment
) {
    public static ReservationResponse from(Reservation reservation) {
        return from(reservation, null);
    }

    public static ReservationResponse from(Reservation reservation, PaymentDetails payment) {
        Slot slot = reservation.slot();
        return new ReservationResponse(
                reservation.id(),
                reservation.owner().name(),
                slot.date(),
                TimeInfo.from(slot.time()),
                ThemeInfo.from(slot.theme()),
                statusFrom(reservation),
                null,
                payment == null ? null : PaymentInfoResponse.from(payment)
        );
    }

    public static ReservationResponse from(WaitingWithRank waitingWithRank) {
        Slot slot = waitingWithRank.waiting().slot();
        return new ReservationResponse(
                waitingWithRank.waiting().id(),
                waitingWithRank.waiting().owner().name(),
                slot.date(),
                TimeInfo.from(slot.time()),
                ThemeInfo.from(slot.theme()),
                ReservationStatus.WAITING,
                waitingWithRank.rank(),
                null
        );
    }

    private static ReservationStatus statusFrom(Reservation reservation) {
        if (reservation.status() == roomescape.domain.ReservationStatus.PENDING_PAYMENT) {
            return ReservationStatus.PENDING_PAYMENT;
        }
        return ReservationStatus.RESERVED;
    }
}
