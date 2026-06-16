package roomescape.reservation.infra;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.domain.PaymentOrder;
import roomescape.reservation.domain.repository.PaymentOrderRepository;

@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcPaymentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservation_id", paymentOrder.getReservationId())
                .addValue("order_id", paymentOrder.getOrderId().value())
                .addValue("amount", paymentOrder.getAmount().value())
                .addValue("status", paymentOrder.getStatus().name());
        try {
            Long id = jdbcInsert.executeAndReturnKey(params).longValue();
            return paymentOrder.withId(id);
        } catch (DuplicateKeyException e) {
            throw new UniqueConstraintViolationException(e);
        }
    }
}
