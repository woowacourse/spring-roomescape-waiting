package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.PendingPayment;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcPendingPaymentRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcPendingPaymentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcPendingPaymentRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("주문을 저장하고 orderId로 조회한다.")
    void saveAndFind() {
        repository.save(new PendingPayment("order-123", 50000L));

        Optional<PendingPayment> found = repository.findByOrderId("order-123");

        assertThat(found).isPresent();
        assertThat(found.get().amount()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("존재하지 않는 orderId 조회 시 빈 Optional을 반환한다.")
    void findNonExistent() {
        Optional<PendingPayment> found = repository.findByOrderId("non-existent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("orderId로 주문을 삭제하면 조회되지 않는다.")
    void deleteByOrderId() {
        repository.save(new PendingPayment("order-123", 50000L));

        repository.deleteByOrderId("order-123");

        assertThat(repository.findByOrderId("order-123")).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 orderId를 삭제해도 예외가 발생하지 않는다.")
    void deleteNonExistent() {
        assertThatCode(() -> repository.deleteByOrderId("non-existent")).doesNotThrowAnyException();
    }
}
