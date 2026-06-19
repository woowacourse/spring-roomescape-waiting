package roomescape.dto.reservation;

import roomescape.domain.payment.Order;
import roomescape.domain.payment.PaymentStatus;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

import java.time.LocalDate;

public class MyReservationResponse {
    private final Long reservationId;
    private final Long waitingId;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final ReservationStatus status;
    private final Long sequence;
    private final String orderId;
    private final String paymentKey;
    private final Long amount;
    private final PaymentStatus paymentStatus;

    public MyReservationResponse(Long reservationId, Long waitingId, LocalDate date, ReservationTimeResponse time,
                                 ThemeResponse theme, ReservationStatus status, Long sequence,
                                 String orderId, String paymentKey, Long amount, PaymentStatus paymentStatus) {
        this.reservationId = reservationId;
        this.waitingId = waitingId;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.sequence = sequence;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    public static MyReservationResponse fromReservation(Reservation reservation, Order order) {
        return new MyReservationResponse(
                reservation.getId(),
                null,
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                ReservationStatus.RESERVED,
                null,
                order.getOrderId(),
                order.getPaymentKey(),
                order.getAmount(),
                order.getPaymentStatus()
        );
    }

    public static MyReservationResponse fromReservationWithoutOrder(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                null,
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                ReservationStatus.RESERVED,
                null,
                null, null, null, null
        );
    }

    public static MyReservationResponse fromWaiting(ReservationWaiting reservationWaiting, Long sequence) {
        return new MyReservationResponse(
                null,
                reservationWaiting.getId(),
                reservationWaiting.getDate(),
                ReservationTimeResponse.from(reservationWaiting.getTime()),
                ThemeResponse.from(reservationWaiting.getTheme()),
                ReservationStatus.WAITING,
                sequence,
                null, null, null, null
        );
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getWaitingId() {
        return waitingId;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getSequence() {
        return sequence;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
}
