package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Order;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Slot;

public class MyReservationResult {

    public enum Status {RESERVED, WAITING}

    private final Long id;
    private final LocalDate date;
    private final ReservationTimeResult time;
    private final ThemeResult theme;
    private final Status status;
    private final Integer waitingOrder;
    private final PaymentStatus paymentStatus;
    private final String orderId;
    private final String paymentKey;
    private final Long amount;

    private MyReservationResult(Long id, LocalDate date,
                                ReservationTimeResult time, ThemeResult theme,
                                Status status, Integer waitingOrder,
                                PaymentStatus paymentStatus, String orderId,
                                String paymentKey, Long amount) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.waitingOrder = waitingOrder;
        this.paymentStatus = paymentStatus;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
    }

    public static MyReservationResult ofReservation(Long id, Slot slot, Order order) {
        return new MyReservationResult(
                id,
                slot.getDate(),
                ReservationTimeResult.from(slot.getTime()),
                ThemeResult.from(slot.getTheme()),
                Status.RESERVED, null,
                order.getStatus(),
                order.getOrderId(),
                order.getPaymentKey(),
                order.getAmount()
        );
    }

    public static MyReservationResult ofReservation(Long id, Slot slot) {
        return new MyReservationResult(
                id,
                slot.getDate(),
                ReservationTimeResult.from(slot.getTime()),
                ThemeResult.from(slot.getTheme()),
                Status.RESERVED, null,
                null, null, null, null
        );
    }

    public static MyReservationResult ofWaiting(Long id, Slot slot, int order) {
        return new MyReservationResult(
                id,
                slot.getDate(),
                ReservationTimeResult.from(slot.getTime()),
                ThemeResult.from(slot.getTheme()),
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

    public Long getAmount() {
        return amount;
    }
}
