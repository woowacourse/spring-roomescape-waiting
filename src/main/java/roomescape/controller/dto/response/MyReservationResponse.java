package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.MyReservation;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPayment;
import roomescape.domain.ReservationType;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        Long waitingNumber,
        ReservationStatus reservationStatus,
        PaymentStatus paymentStatus,
        String orderId,
        String paymentKey,
        Long amount
) {
    public static MyReservationResponse from(MyReservation myReservation) {
        Reservation reservation = myReservation.reservation();
        Theme theme = reservation.getTheme();
        ReservationPayment payment = myReservation.payment();

        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                reservation.getTime().getStartAt(),
                myReservation.waitingNumber(),
                reservationStatus(myReservation),
                payment == null ? null : payment.getPaymentStatus(),
                payment == null ? null : payment.getOrderId(),
                payment == null ? null : payment.getPaymentKey(),
                payment == null ? null : payment.getAmount()
        );
    }

    private static ReservationStatus reservationStatus(MyReservation myReservation) {
        if (myReservation.reservationType() == ReservationType.PAYMENT) {
            return ReservationStatus.PAYMENT_PENDING;
        }
        return ReservationStatus.valueOf(myReservation.reservationType().name());
    }
}
