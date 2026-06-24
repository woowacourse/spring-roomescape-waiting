package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private Long amount;
    private String paymentKey;
    private String idempotencyKey;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    protected Order() {
    }

    public Order(Long id, String orderId, Long amount, Reservation reservation) {
        Objects.requireNonNull(orderId, "주문 id는 필수값 입니다.");
        Objects.requireNonNull(amount, "결제 금액은 필수값 입니다.");
        Objects.requireNonNull(reservation, "예약은 필수값 입니다.");
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.reservation = reservation;
        this.idempotencyKey = UUID.randomUUID().toString();
    }

    public static Order createWithoutId(String orderId, Long amount, Reservation reservation) {
        return new Order(null, orderId, amount, reservation);
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public void updatePaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Order order = (Order) object;
        if (id != null && order.id != null) {
            return Objects.equals(id, order.id);
        }
        return Objects.equals(orderId, order.orderId)
                && Objects.equals(amount, order.amount)
                && Objects.equals(reservation, order.reservation);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(orderId, amount, reservation);
    }
}
