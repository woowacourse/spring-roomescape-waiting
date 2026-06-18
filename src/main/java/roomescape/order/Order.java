package roomescape.order;

import roomescape.common.DomainAssert;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.PaymentAmountMismatchException;

/**
 * 결제 대상 주문. 예약 생성(결제 전) 시점에 orderId와 최종 amount를 함께 박아 둔다.
 * amount는 이 시점의 스냅샷이며, 이후 테마 가격이 바뀌어도 검증 기준은 항상 여기 저장된 값이다.
 * paymentKey는 승인 성공 후 토스로부터 받아 합류한다.
 * idempotencyKey는 주문당 고정 — confirm 재시도가 토스에서 이중 승인되지 않도록 헤더로 보낼 멱등키다.
 */
public class Order {
    private final Long id;
    private final String orderId;
    private final String idempotencyKey;
    private final Long reservationId;
    private final long amount;
    private String paymentKey;
    private OrderStatus status;

    private Order(Long id, String orderId, String idempotencyKey, Long reservationId, long amount,
                  String paymentKey, OrderStatus status) {
        DomainAssert.notNull(orderId, "주문 번호는 비어 있을 수 없습니다.");
        DomainAssert.notNull(idempotencyKey, "멱등키는 비어 있을 수 없습니다.");
        DomainAssert.notNull(reservationId, "예약 식별자는 비어 있을 수 없습니다.");
        DomainAssert.notNull(status, "주문 상태는 비어 있을 수 없습니다.");
        this.id = id;
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.status = status;
    }

    public static Order create(String orderId, String idempotencyKey, Long reservationId, long amount) {
        return new Order(null, orderId, idempotencyKey, reservationId, amount, null, OrderStatus.PENDING);
    }

    public static Order reconstruct(Long id, String orderId, String idempotencyKey, Long reservationId,
                                    long amount, String paymentKey, OrderStatus status) {
        return new Order(id, orderId, idempotencyKey, reservationId, amount, paymentKey, status);
    }

    /**
     * 콜백으로 넘어온 금액이 저장된 주문 금액과 일치하는지 검증한다. 승인 호출 *전에* 호출되어,
     * 조작된 금액이 게이트웨이까지 도달하지 못하게 막는다.
     */
    public void validateAmount(long requestedAmount) {
        if (this.amount != requestedAmount) {
            throw new PaymentAmountMismatchException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }
    }

    public void complete(String paymentKey) {
        DomainAssert.notNull(paymentKey, "결제 키는 비어 있을 수 없습니다.");
        if (this.status == OrderStatus.CONFIRMED) {
            throw new BusinessRuleViolationException("이미 결제가 완료된 주문입니다.");
        }
        this.paymentKey = paymentKey;
        this.status = OrderStatus.CONFIRMED;
    }

    public void markFailed() {
        this.status = OrderStatus.FAILED;
    }

    /**
     * 승인 결과가 불명확한 상태로 표시한다(read timeout 등). 실패가 아니라 '확인 필요' —
     * reaper가 건드리지 않고, 멱등 재시도로 결과를 확정한다.
     * 재확인(recheck) 때 같은 요청을 다시 보낼 수 있도록 시도했던 paymentKey를 함께 저장한다.
     */
    public void markNeedsCheck(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = OrderStatus.NEEDS_CHECK;
    }

    public boolean isConfirmed() {
        return status == OrderStatus.CONFIRMED;
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
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

    public Long getReservationId() {
        return reservationId;
    }

    public long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
