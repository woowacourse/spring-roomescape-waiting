package roomescape.feature.payment.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.feature.payment.domain.Payment;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcPaymentRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("payment")
                .usingColumns("reservation_id", "order_id", "payment_key", "amount")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Payment save(Payment payment) {
        Map<String, Object> args = Map.of(
                "reservation_id", payment.getReservationId(),
                "order_id", payment.getOrderId(),
                "payment_key", payment.getPaymentKey(),
                "amount", payment.getAmount()
        );
        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(args).longValue();

        return Payment.reconstruct(generatedKey, payment.getReservationId(), payment.getOrderId(),
                payment.getPaymentKey(), payment.getAmount());
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = """
                SELECT id, reservation_id, order_id, payment_key, amount
                FROM payment
                WHERE order_id = :orderId
                """;
        SqlParameterSource parameters = new MapSqlParameterSource("orderId", orderId);
        List<Payment> payments = jdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapPayment(rs));

        return payments.stream().findFirst();
    }

    @Override
    public List<Payment> findByReservationIds(List<Long> reservationIds) {
        if (reservationIds.isEmpty()) {
            return List.of();
        }
        String sql = """
                SELECT id, reservation_id, order_id, payment_key, amount
                FROM payment
                WHERE reservation_id IN (:reservationIds)
                """;
        SqlParameterSource parameters = new MapSqlParameterSource("reservationIds", reservationIds);

        return jdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapPayment(rs));
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return Payment.reconstruct(
                rs.getLong("id"),
                rs.getLong("reservation_id"),
                rs.getString("order_id"),
                rs.getString("payment_key"),
                rs.getLong("amount")
        );
    }
}
