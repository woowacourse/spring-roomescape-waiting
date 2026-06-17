package roomescape.reservation.domain;

import roomescape.reservation.exception.ForbiddenRequestException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;

public class Reservation {
    private final Long id;
    private final String name;
    private final ReservationTime time;
    private final Theme theme;
    private final Status status;
    private final String orderId;
    private final Long amount;
    private final String paymentKey;
    private final LocalDateTime createdAt;

    public Reservation(String name, ReservationTime time, Theme theme, Status status, String orderId, Long amount, LocalDateTime createdAt) {
        this(null, name, time, theme, status, orderId, amount, null, createdAt);
    }

    private Reservation(Long id, String name, ReservationTime time, Theme theme, Status status,
                        String orderId, Long amount, String paymentKey, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.createdAt = createdAt;
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.name, this.time, this.theme, this.status, this.orderId, this.amount, this.paymentKey, this.createdAt);
    }

    public Reservation withTimeAndStatus(ReservationTime time, Status status) {
        return new Reservation(this.id, this.name, time, this.theme, status, this.orderId, this.amount, this.paymentKey, this.createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isReserved() {
        return this.status.holdsSlot();
    }

    public void validateOwnedBy(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenRequestException();
        }
    }
}
