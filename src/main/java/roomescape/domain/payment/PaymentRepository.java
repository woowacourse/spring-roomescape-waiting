package roomescape.domain.payment;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<ReservationPayment> rowMapper = (resultSet, rowNum) ->
        new ReservationPayment(
            resultSet.getLong("id"),
            resultSet.getLong("reservation_id"),
            resultSet.getString("order_id"),
            resultSet.getString("payment_key"),
            resultSet.getLong("amount"),
            resultSet.getString("idempotency_key"),
            PaymentStatus.valueOf(resultSet.getString("status"))
        );

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("reservation_payment")
            .usingGeneratedKeyColumns("id");
    }

    public ReservationPayment save(ReservationPayment payment) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("reservation_id", payment.reservationId())
            .addValue("order_id", payment.orderId())
            .addValue("payment_key", payment.paymentKey())
            .addValue("amount", payment.amount())
            .addValue("idempotency_key", payment.idempotencyKey())
            .addValue("status", payment.status().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new ReservationPayment(
            id,
            payment.reservationId(),
            payment.orderId(),
            payment.paymentKey(),
            payment.amount(),
            payment.idempotencyKey(),
            payment.status()
        );
    }

    public Optional<ReservationPayment> findByOrderId(String orderId) {
        String query = """
            SELECT id, reservation_id, order_id, payment_key, amount, idempotency_key, status
            FROM reservation_payment
            WHERE order_id = ?
            """;
        return jdbcTemplate.query(query, rowMapper, orderId).stream().findFirst();
    }

    public List<ReservationPayment> findByReservationIds(List<Long> reservationIds) {
        if (reservationIds.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", reservationIds.stream().map(id -> "?").toList());
        String query = """
            SELECT id, reservation_id, order_id, payment_key, amount, idempotency_key, status
            FROM reservation_payment
            WHERE reservation_id IN (%s)
            """.formatted(placeholders);
        return jdbcTemplate.query(query, rowMapper, reservationIds.toArray());
    }

    public void updateConfirmed(String orderId, String paymentKey, Long amount) {
        String query = """
            UPDATE reservation_payment
            SET payment_key = ?, amount = ?, status = ?
            WHERE order_id = ?
            """;
        jdbcTemplate.update(query, paymentKey, amount, PaymentStatus.CONFIRMED.name(), orderId);
    }

    public void updateStatus(String orderId, PaymentStatus status) {
        String query = "UPDATE reservation_payment SET status = ? WHERE order_id = ?";
        jdbcTemplate.update(query, status.name(), orderId);
    }

    public void updateRequiresConfirmation(String orderId, String paymentKey) {
        String query = """
            UPDATE reservation_payment
            SET payment_key = ?, status = ?
            WHERE order_id = ?
            """;
        jdbcTemplate.update(
            query,
            paymentKey,
            PaymentStatus.REQUIRES_CONFIRMATION.name(),
            orderId
        );
    }
}
