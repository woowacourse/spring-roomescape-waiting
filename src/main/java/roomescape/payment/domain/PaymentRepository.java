package roomescape.payment.domain;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private static final RowMapper<Payment> ROW_MAPPER = (rs, rowNum) -> Payment.of(
            rs.getString("payment_key"),
            rs.getString("order_id"),
            rs.getLong("amount"),
            rs.getString("status"),
            rs.getLong("reservation_id")
    );

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment")
                .usingGeneratedKeyColumns("id");
    }

    public void save(Payment payment) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("payment_key", payment.getPaymentKey())
                .addValue("order_id", payment.getOrderId())
                .addValue("amount", payment.getAmount())
                .addValue("status", payment.getStatus())
                .addValue("reservation_id", payment.getReservationId());
        simpleJdbcInsert.execute(parameters);
    }

    public Optional<Payment> findByReservationId(Long reservationId) {
        String sql = "SELECT * FROM payment WHERE reservation_id = ?";
        return jdbcTemplate.query(sql, ROW_MAPPER, reservationId)
                .stream()
                .findFirst();
    }

    public void deleteByReservationId(Long reservationId) {
        jdbcTemplate.update("DELETE FROM payment WHERE reservation_id = ?", reservationId);
    }
}