package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcPaymentOrderRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcPaymentOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcPaymentOrderRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("주문을 저장하고 orderId로 조회한다.")
    void saveAndFind() {
        repository.save(PaymentOrder.prepare("order-123", 50000L));

        Optional<PaymentOrder> found = repository.findByOrderId("order-123");

        assertThat(found).isPresent();
        assertThat(found.get().amount()).isEqualTo(50000L);
        assertThat(found.get().status()).isEqualTo(PaymentOrderStatus.PENDING);
        assertThat(found.get().idempotencyKey()).isNotBlank();
    }

    @Test
    @DisplayName("존재하지 않는 orderId 조회 시 빈 Optional을 반환한다.")
    void findNonExistent() {
        Optional<PaymentOrder> found = repository.findByOrderId("non-existent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("주문 상태/이름/결제키를 갱신한다.")
    void update() {
        repository.save(PaymentOrder.prepare("order-123", 50000L));

        PaymentOrder confirmed = repository.findByOrderId("order-123").orElseThrow()
                .confirmed("브라운", null, "pk_test");
        repository.update(confirmed);

        PaymentOrder found = repository.findByOrderId("order-123").orElseThrow();
        assertThat(found.status()).isEqualTo(PaymentOrderStatus.CONFIRMED);
        assertThat(found.name()).isEqualTo("브라운");
        assertThat(found.paymentKey()).isEqualTo("pk_test");
    }

    @Test
    @DisplayName("사용자 이름으로 주문 내역을 조회한다.")
    void findByName() {
        repository.save(PaymentOrder.prepare("order-1", 1000L).failed("브라운", null));
        repository.save(PaymentOrder.prepare("order-2", 2000L).unknown("브라운", null, "pk_2"));
        repository.save(PaymentOrder.prepare("order-3", 3000L).confirmed("네오", null, "pk_3"));

        List<PaymentOrder> orders = repository.findByName("브라운");

        assertThat(orders).hasSize(2)
                .extracting(PaymentOrder::orderId)
                .containsExactlyInAnyOrder("order-1", "order-2");
    }

    @Test
    @DisplayName("orderId로 주문을 삭제하면 조회되지 않는다.")
    void deleteByOrderId() {
        repository.save(PaymentOrder.prepare("order-123", 50000L));

        repository.deleteByOrderId("order-123");

        assertThat(repository.findByOrderId("order-123")).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 orderId를 삭제해도 예외가 발생하지 않는다.")
    void deleteNonExistent() {
        assertThatCode(() -> repository.deleteByOrderId("non-existent")).doesNotThrowAnyException();
    }
}
