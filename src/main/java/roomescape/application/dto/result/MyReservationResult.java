package roomescape.application.dto.result;

import java.time.LocalDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentStatus;

public class MyReservationResult {

    public enum Status {RESERVED, WAITING}

    private final Long id;
    private final LocalDate date;
    private final ReservationTimeResult time;
    private final ThemeResult theme;
    private final Status status;
    private final Integer waitingOrder;
    private final PaymentStatus paymentStatus; // 예약 행에서만(결제 없으면 null)
    private final String orderId;
    private final String paymentKey;
    private final Long paymentAmount;

    private MyReservationResult(Long id, LocalDate date,
                                ReservationTimeResult time, ThemeResult theme,
                                Status status, Integer waitingOrder,
                                PaymentStatus paymentStatus, String orderId,
                                String paymentKey, Long paymentAmount) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.waitingOrder = waitingOrder;
        this.paymentStatus = paymentStatus;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.paymentAmount = paymentAmount;
    }

    public static MyReservationResult ofReservation(Long id, LocalDate date,
                                                    ReservationTime time, Theme theme,
                                                    Payment payment) {
        return new MyReservationResult(
                id, date, ReservationTimeResult.from(time), ThemeResult.from(theme),
                Status.RESERVED, null,
                payment == null ? null : payment.getStatus(),
                payment == null ? null : payment.getOrderId(),
                payment == null ? null : payment.getPaymentKey(),
                payment == null ? null : payment.getAmount()
        );
    }

    public static MyReservationResult ofWaiting(Long id, LocalDate date,
                                                ReservationTime time, Theme theme, int order) {
        return new MyReservationResult(
                id, date, ReservationTimeResult.from(time), ThemeResult.from(theme),
                Status.WAITING, order,
                null, null, null, null
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResult getTime() {
        return time;
    }

    public ThemeResult getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }

    public Integer getWaitingOrder() {
        return waitingOrder;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getPaymentAmount() {
        return paymentAmount;
    }
}
