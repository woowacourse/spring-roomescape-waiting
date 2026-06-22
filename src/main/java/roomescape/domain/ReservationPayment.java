package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import roomescape.domain.exception.InvalidInputException;

public class ReservationPayment {

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{6,64}$");

    private final Long id;
    private final String orderId;
    private final String idempotencyKey;
    private final long amount;
    private final String paymentKey;
    private final PaymentStatus paymentStatus;
    private final String failureCode;
    private final String failureMessage;
    private final Reservation reservation;

    public ReservationPayment(Long id, String orderId, long amount, String paymentKey, Reservation reservation) {
        this(id, orderId, UUID.randomUUID().toString(), amount, paymentKey, PaymentStatus.PENDING, null, null,
                reservation);
    }

    public ReservationPayment(
            Long id,
            String orderId,
            String idempotencyKey,
            long amount,
            String paymentKey,
            PaymentStatus paymentStatus,
            String failureCode,
            String failureMessage,
            Reservation reservation
    ) {
        validate(orderId, idempotencyKey, amount, paymentStatus, reservation);
        this.id = id;
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.paymentStatus = paymentStatus;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.reservation = reservation;
    }

    public ReservationPayment(String orderId, long amount, Reservation reservation) {
        this(null, orderId, UUID.randomUUID().toString(), amount, null, PaymentStatus.PENDING, null, null,
                reservation);
    }

    public ReservationPayment withId(long id) {
        return new ReservationPayment(id, orderId, idempotencyKey, amount, paymentKey, paymentStatus, failureCode,
                failureMessage, reservation);
    }

    public ReservationPayment withPaymentKey(String paymentKey) {
        return new ReservationPayment(id, orderId, idempotencyKey, amount, paymentKey, paymentStatus, failureCode,
                failureMessage, reservation);
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public LocalDateTime getCreatedAt() {
        return reservation.getCreatedAt();
    }

    private void validate(String orderId, String idempotencyKey, long amount, PaymentStatus paymentStatus,
                          Reservation reservation) {
        validateOrderId(orderId);
        validateIdempotencyKey(idempotencyKey);
        validateAmount(amount);
        validatePaymentStatus(paymentStatus);
        validateReservation(reservation);
    }

    private void validateOrderId(String orderId) {
        if (orderId == null || !ORDER_ID_PATTERN.matcher(orderId).matches()) {
            throw new InvalidInputException("주문 번호는 6~64자의 영문, 숫자, 하이픈, 언더스코어만 사용할 수 있습니다.");
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 300) {
            throw new InvalidInputException("결제 멱등키는 1~300자여야 합니다.");
        }
    }

    private void validateAmount(long amount) {
        if (amount <= 0) {
            throw new InvalidInputException("결제 금액은 0보다 커야 합니다.");
        }
    }

    private void validatePaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new InvalidInputException("결제 상태는 필수입니다.");
        }
    }

    private void validateReservation(Reservation reservation) {
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
