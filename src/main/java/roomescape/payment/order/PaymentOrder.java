package roomescape.payment.order;

import java.time.LocalDate;
import roomescape.service.dto.ReservationCreateCommand;

public class PaymentOrder {

    private final String orderId;
    private final String reserverName;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;
    private final Long amount;
    private final String paymentKey;
    private final PaymentOrderStatus status;
    private final Long reservationId;

    public PaymentOrder(String orderId, String reserverName, LocalDate date, Long timeId, Long themeId,
                        Long amount, String paymentKey, PaymentOrderStatus status, Long reservationId) {
        this.orderId = orderId;
        this.reserverName = reserverName;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
        this.reservationId = reservationId;
    }

    public static PaymentOrder pending(String orderId, ReservationCreateCommand command, Long amount) {
        return new PaymentOrder(
                orderId,
                command.reserverName(),
                command.date(),
                command.timeId(),
                command.themeId(),
                amount,
                null,
                PaymentOrderStatus.PENDING,
                null
        );
    }

    public ReservationCreateCommand toCommand() {
        return new ReservationCreateCommand(reserverName, date, timeId, themeId);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getReserverName() {
        return reserverName;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public PaymentOrderStatus getStatus() {
        return status;
    }

    public Long getReservationId() {
        return reservationId;
    }
}
