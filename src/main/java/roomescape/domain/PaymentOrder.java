package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.Objects;

@Entity
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private Long amount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    protected PaymentOrder() {
    }

    public PaymentOrder(Long id, String orderId, Long amount, Reservation reservation) {
        Objects.requireNonNull(orderId, "주문 id는 필수값 입니다.");
        Objects.requireNonNull(amount, "결제 금액은 필수값 입니다.");
        Objects.requireNonNull(reservation, "예약은 필수값 입니다.");
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.reservation = reservation;
    }

    public static PaymentOrder createWithoutId(String orderId, Long amount, Reservation reservation) {
        return new PaymentOrder(null, orderId, amount, reservation);
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        PaymentOrder paymentOrder = (PaymentOrder) object;
        if (id != null && paymentOrder.id != null) {
            return Objects.equals(id, paymentOrder.id);
        }
        return Objects.equals(orderId, paymentOrder.orderId)
                && Objects.equals(amount, paymentOrder.amount)
                && Objects.equals(reservation, paymentOrder.reservation);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(orderId, amount, reservation);
    }
}
