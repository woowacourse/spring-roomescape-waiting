package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.domain.reservationOrder.ReservationOrderRepository;

@JdbcTest
class ReservationOrderDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationOrderRepository orderDao;

    @BeforeEach
    void setUp() {
        this.orderDao = new JdbcReservationOrderRepository(jdbcTemplate);
        jdbcTemplate.update("delete from reservation_order");
    }

    @Test
    void 주문을_저장하면_미결제_상태로_조회된다() {
        orderDao.insert(ReservationOrder.restore("order-1", 10000, null, 1L));

        Optional<ReservationOrder> found = orderDao.findById("order-1");

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualTo(10000);
        assertThat(found.get().getReservationId()).isEqualTo(1L);
        assertThat(found.get().getPaymentKey()).isNull();
        assertThat(found.get().isConfirmed()).isFalse();
    }

    @Test
    void 존재하지_않는_주문은_빈_Optional을_반환한다() {
        assertThat(orderDao.findById("no-order")).isEmpty();
    }

    @Test
    void 같은_예약에_주문을_두_개_저장하면_예외가_발생한다() {
        orderDao.insert(ReservationOrder.restore("order-1", 10000, null, 1L));

        assertThatThrownBy(() -> orderDao.insert(ReservationOrder.restore("order-2", 10000, null, 1L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void updatePaymentKey는_올바른_주문에_paymentKey를_저장한다() {
        orderDao.insert(ReservationOrder.restore("order-1", 10000, null, 1L));

        orderDao.updatePaymentKey("order-1", "pk_test");

        ReservationOrder found = orderDao.findById("order-1").orElseThrow();
        assertThat(found.getPaymentKey()).isEqualTo("pk_test");
        assertThat(found.isConfirmed()).isTrue();
    }

    @Test
    void findById는_저장된_paymentKey_컬럼을_복원한다() {
        orderDao.insert(ReservationOrder.restore("order-1", 10000, null, 1L));
        orderDao.updatePaymentKey("order-1", "pk_test");

        assertThat(orderDao.findById("order-1").orElseThrow().getPaymentKey()).isEqualTo("pk_test");
    }
}
