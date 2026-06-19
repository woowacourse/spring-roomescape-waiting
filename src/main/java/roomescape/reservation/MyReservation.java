package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.payment.Payment;
import roomescape.theme.Theme;
import roomescape.waiting.ReservationWaiting;

public class MyReservation {
    private static final String RESERVED_RESOURCE = "reservation";
    private static final String WAITING_RESOURCE = "waiting";
    private static final String WAITING_STATUS = "대기중";

    private final Long id;
    private final String name;
    private final String themeName;
    private final LocalDate date;
    private final LocalTime startAt;
    private final String resourceType;
    private final String status;
    private final Long waitingNumber;
    private final String paymentStatus;
    private final String orderId;
    private final String paymentKey;
    private final Long amount;

    public MyReservation(Reservation reservation, Theme theme) {
        this(reservation, theme, null);
    }

    public MyReservation(Reservation reservation, Theme theme, Payment payment) {
        this.id = reservation.getId();
        this.name = reservation.getName();
        this.themeName = theme.getName();
        this.date = reservation.getDate();
        this.startAt = reservation.getTime().getStartAt();
        this.resourceType = RESERVED_RESOURCE;
        this.status = reservation.getStatus().getDescription();
        this.waitingNumber = null;
        this.paymentStatus = payment == null ? null : payment.getStatus().getDescription();
        this.orderId = payment == null ? null : payment.getOrderId();
        this.paymentKey = payment == null ? null : payment.getPaymentKey();
        this.amount = payment == null ? null : payment.getAmount();
    }

    public MyReservation(ReservationWaiting waiting, Theme theme) {
        this.id = waiting.getId();
        this.name = waiting.getName();
        this.themeName = theme.getName();
        this.date = waiting.getDate();
        this.startAt = waiting.getTime().getStartAt();
        this.resourceType = WAITING_RESOURCE;
        this.status = WAITING_STATUS;
        this.waitingNumber = waiting.getWaitingNumber();
        this.paymentStatus = null;
        this.orderId = null;
        this.paymentKey = null;
        this.amount = null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getThemeName() {
        return themeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getStatus() {
        return status;
    }

    public Long getWaitingNumber() {
        return waitingNumber;
    }

    public String getPaymentStatus() {
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
