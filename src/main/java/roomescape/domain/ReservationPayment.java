package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.domain.exception.InvalidInputException;

public class ReservationPayment {

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{6,64}$");

    private final Long id;
    private final String orderId;
    private final long amount;
    private final String paymentKey;
    private final Reservation reservation;

    public ReservationPayment(Long id, String orderId, long amount, String paymentKey, Reservation reservation) {
        validate(orderId, amount, reservation);
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.reservation = reservation;
    }

    public ReservationPayment(String orderId, long amount, Reservation reservation) {
        this(null, orderId, amount, null, reservation);
    }

    public ReservationPayment withId(long id) {
        return new ReservationPayment(id, orderId, amount, paymentKey, reservation);
    }

    public ReservationPayment withPaymentKey(String paymentKey) {
        return new ReservationPayment(id, orderId, amount, paymentKey, reservation);
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public LocalDateTime getCreatedAt() {
        return reservation.getCreatedAt();
    }

    private void validate(String orderId, long amount, Reservation reservation) {
        if (orderId == null || !ORDER_ID_PATTERN.matcher(orderId).matches()) {
            throw new InvalidInputException("주문 번호는 6~64자의 영문, 숫자, 하이픈, 언더스코어만 사용할 수 있습니다.");
        }
        if (amount <= 0) {
            throw new InvalidInputException("결제 금액은 0보다 커야 합니다.");
        }
        if (reservation == null) {
            throw new InvalidInputException("예약 정보는 필수입니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationPayment that)) return false;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        if (id != null) return Objects.hash(id);
        return Objects.hash(orderId);
    }
}
