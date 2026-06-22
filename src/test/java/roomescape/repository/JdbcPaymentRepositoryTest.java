package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.payment.Payment;
import roomescape.payment.PaymentOrderStatus;

@JdbcTest
@Import(JdbcPaymentRepository.class)
class JdbcPaymentRepositoryTest {

    @Autowired
    private JdbcPaymentRepository paymentRepository;

    @Test
    @DisplayName("주문을 저장하면 id가 채번되고 orderId로 조회된다")
    void save_findByOrderId() {
        Payment saved = paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null, PaymentOrderStatus.PENDING));

        assertThat(saved.id()).isNotNull();

        Payment found = paymentRepository.findByOrderId("order-xyz").orElseThrow();
        assertThat(found.reservationId()).isEqualTo(1L);
        assertThat(found.amount()).isEqualTo(30_000L);
        assertThat(found.paymentKey()).isNull();
    }

    @Test
    @DisplayName("payment_key를 갱신하면 승인 표시가 채워진다")
    void updatePaymentKey() {
        paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null, PaymentOrderStatus.PENDING));

        paymentRepository.updatePaymentKey("order-xyz", "test_payment_key");

        Payment found = paymentRepository.findByOrderId("order-xyz").orElseThrow();
        assertThat(found.paymentKey()).isEqualTo("test_payment_key");
    }

    @Test
    @DisplayName("존재하지 않는 orderId 조회 시 빈 Optional 을 반환한다")
    void findByOrderId_notFound() {
        assertThat(paymentRepository.findByOrderId("order-none")).isEmpty();
    }

    @Test
    @DisplayName("reservationId 로 주문을 조회한다")
    void findByReservationId() {
        paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null, PaymentOrderStatus.PENDING));

        Payment found = paymentRepository.findByReservationId(1L).orElseThrow();
        assertThat(found.orderId()).isEqualTo("order-xyz");
        assertThat(found.reservationId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 reservationId 조회 시 빈 Optional 을 반환한다")
    void findByReservationId_notFound() {
        assertThat(paymentRepository.findByReservationId(999L)).isEmpty();
    }

    @Test
    @DisplayName("orderId로 주문을 삭제한다")
    void deleteByOrderId() {
        paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null, PaymentOrderStatus.PENDING));

        paymentRepository.deleteByOrderId("order-xyz");

        assertThat(paymentRepository.findByOrderId("order-xyz")).isEmpty();
    }

    @Test
    @DisplayName("주문 상태를 갱신한다")
    void updateStatus() {
        paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null, PaymentOrderStatus.PENDING));

        paymentRepository.updateStatus("order-xyz", PaymentOrderStatus.NEEDS_CONFIRMATION);

        Payment found = paymentRepository.findByOrderId("order-xyz").orElseThrow();
        assertThat(found.status()).isEqualTo(PaymentOrderStatus.NEEDS_CONFIRMATION);
    }

    @Test
    @DisplayName("여러 reservationId로 결제 정보를 한 번에 조회한다")
    void findByReservationIds() {
        paymentRepository.save(new Payment(null, 1L, "order-1", 30_000L, null, PaymentOrderStatus.PENDING));
        paymentRepository.save(new Payment(null, 2L, "order-2", 30_000L, null, PaymentOrderStatus.PENDING));

        List<Payment> found = paymentRepository.findByReservationIds(List.of(1L, 2L, 999L));

        assertThat(found).hasSize(2)
                .extracting(Payment::orderId)
                .containsExactlyInAnyOrder("order-1", "order-2");
    }

    @Test
    @DisplayName("빈 reservationId 목록을 조회하면 빈 리스트를 반환한다")
    void findByReservationIds_empty() {
        assertThat(paymentRepository.findByReservationIds(List.of())).isEmpty();
    }
}
