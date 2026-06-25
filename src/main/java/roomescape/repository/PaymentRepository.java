package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Payment> paymentRowMapper = (resultSet, rowNum) -> Payment.restore(
            resultSet.getLong("id"),
            resultSet.getLong("reservation_id"),
            resultSet.getString("order_id"),
            resultSet.getLong("amount"),
            resultSet.getString("payment_key"),
            PaymentStatus.valueOf(resultSet.getString("status")),
            resultSet.getString("failure_code"),
            resultSet.getString("failure_message")
    );

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Payment insert(Payment payment) {
        String sql = """
                INSERT INTO payment(reservation_id, order_id, amount, payment_key, status, failure_code, failure_message)
                VALUES (?, ?, ?, ?, ?, ?, ?);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setLong(1, payment.getReservationId());
            statement.setString(2, payment.getOrderId());
            statement.setLong(3, payment.getAmount());
            statement.setString(4, payment.getPaymentKey());
            statement.setString(5, payment.getStatus().name());
            statement.setString(6, payment.getFailureCode());
            statement.setString(7, payment.getFailureMessage());
            return statement;
        }, keyHolder);
        return payment.withId(keyHolder.getKey().longValue());
    }

    public Optional<Payment> findById(Long id) {
        String sql = "SELECT * FROM payment WHERE id = ?;";
        return jdbcTemplate.query(sql, paymentRowMapper, id).stream().findFirst();
    }

    public Optional<Payment> findByOrderId(String orderId) {
        String sql = "SELECT * FROM payment WHERE order_id = ?;";
        return jdbcTemplate.query(sql, paymentRowMapper, orderId).stream().findFirst();
    }

    public Optional<Payment> findLatestByReservationId(Long reservationId) {
        String sql = "SELECT * FROM payment WHERE reservation_id = ? ORDER BY id DESC LIMIT 1;";
        return jdbcTemplate.query(sql, paymentRowMapper, reservationId).stream().findFirst();
    }

    public int update(Payment payment) {
        String sql = """
                UPDATE payment
                SET payment_key = ?, status = ?, failure_code = ?, failure_message = ?
                WHERE id = ?;
                """;
        return jdbcTemplate.update(sql,
                payment.getPaymentKey(),
                payment.getStatus().name(),
                payment.getFailureCode(),
                payment.getFailureMessage(),
                payment.getId());
    }
}
