package roomescape.domain.payment;

import roomescape.domain.exception.InvalidDomainException;

/**
 * 결제(주문) 영속 모델. 결제 인증 전 주문 정보(orderId, amount)로 생성되고, 승인 성공 시 paymentKey와 결과 상태가 채워진다.
 */
public class Payment {

    private final Long id;
    private final Long reservationId;
    private final String orderId;
    private final long amount;
    private final String paymentKey;   // 승인 전에는 null
    private final PaymentStatus status;

    private Payment(Long id, Long reservationId, String orderId, long amount,
                    String paymentKey, PaymentStatus status) {
        validate(reservationId, orderId, amount, status);
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
    }

    /**
     * 결제 인증 전, 주문 정보만으로 생성(결제 대기).
     */
    public static Payment pending(Long reservationId, String orderId, long amount) {
        return new Payment(null, reservationId, orderId, amount, null, PaymentStatus.PENDING);
    }

    /**
     * DB 복원용.
     */
    public static Payment withId(Long id, Long reservationId, String orderId, long amount,
                                 String paymentKey, PaymentStatus status) {
        return new Payment(id, reservationId, orderId, amount, paymentKey, status);
    }

    /**
     * 승인 성공 결과 반영(대기 상태에서만 가능 — 중복 승인 방지 불변식).
     */
    public Payment confirm(String paymentKey, PaymentStatus resultStatus) {
        if (status != PaymentStatus.PENDING) {
            throw new InvalidDomainException("이미 처리된 결제는 다시 승인할 수 없습니다.");
        }
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new InvalidDomainException("승인된 결제의 paymentKey는 비어 있을 수 없습니다.");
        }
        return new Payment(id, reservationId, orderId, amount, paymentKey, resultStatus);
    }

    private static void validate(Long reservationId, String orderId, long amount, PaymentStatus status) {
        if (reservationId == null) {
            throw new InvalidDomainException("결제는 예약에 연결되어야 합니다.");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new InvalidDomainException("orderId는 비어 있을 수 없습니다.");
        }
        if (amount <= 0) {
            throw new InvalidDomainException("결제 금액은 0보다 커야 합니다.");
        }
        if (status == null) {
            throw new InvalidDomainException("결제 상태는 비어 있을 수 없습니다.");
        }
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
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

    public PaymentStatus getStatus() {
        return status;
    }
}
