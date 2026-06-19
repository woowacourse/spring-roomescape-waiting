package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Payment;

@JdbcTest
@Import(JdbcPaymentRepository.class)
class JdbcPaymentRepositoryTest {

    @Autowired
    private JdbcPaymentRepository paymentRepository;

    @Test
    @DisplayName("주문을 저장하면 id가 채번되고 orderId로 조회된다")
    void save_findByOrderId() {
        Payment saved = paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null));

        assertThat(saved.id()).isNotNull();

        Payment found = paymentRepository.findByOrderId("order-xyz").orElseThrow();
        assertThat(found.reservationId()).isEqualTo(1L);
        assertThat(found.amount()).isEqualTo(30_000L);
        assertThat(found.paymentKey()).isNull();
    }

    @Test
    @DisplayName("payment_key를 갱신하면 승인 표시가 채워진다")
    void updatePaymentKey() {
        paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null));

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
        paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null));

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
        paymentRepository.save(new Payment(null, 1L, "order-xyz", 30_000L, null));

        paymentRepository.deleteByOrderId("order-xyz");

        assertThat(paymentRepository.findByOrderId("order-xyz")).isEmpty();
    }
}
