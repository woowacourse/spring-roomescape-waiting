package roomescape.payment.repository;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.exception.business.BusinessException;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentState;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Payment> rowMapper = (resultSet, rowNum) -> Payment.restore(
            resultSet.getLong("id"),
            resultSet.getLong("reservation_id"),
            resultSet.getString("order_id"),
            resultSet.getString("payment_key"),
            resultSet.getLong("amount"),
            PaymentState.valueOf(resultSet.getString("status"))
    );

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment")
                .usingGeneratedKeyColumns("id")
                .usingColumns("reservation_id", "order_id", "amount", "status");
    }

    @Override
    public Payment save(Payment payment) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("reservation_id", payment.getReservationId())
                .addValue("order_id", payment.getOrderId())
                .addValue("amount", payment.getAmount())
                .addValue("status", payment.getState().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Payment.restore(id, payment.getReservationId(), payment.getOrderId(), payment.getPaymentKey(),
                payment.getAmount(), payment.getState());
    }

    @Override
    public Payment getByOrderId(String orderId) {
        String query = "SELECT * FROM payment WHERE order_id = ?";
        return jdbcTemplate.query(query, rowMapper, orderId).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));
    }

    @Override
    public Optional<Payment> findByReservationId(Long reservationId) {
        String query = "SELECT * FROM payment WHERE reservation_id = ?";
        return jdbcTemplate.query(query, rowMapper, reservationId).stream().findFirst();
    }

    @Override
    public void confirm(String orderId, String paymentKey) {
        String query = "UPDATE payment SET payment_key = ?, status = 'CONFIRMED' WHERE order_id = ?";
        jdbcTemplate.update(query, paymentKey, orderId);
    }

    @Override
    public void deleteByOrderId(String orderId) {
        String query = "DELETE FROM payment WHERE order_id = ?";
        jdbcTemplate.update(query, orderId);
    }
}