package roomescape.payment.repository;

import static java.sql.Timestamp.valueOf;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.NotFoundException;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentState;
import roomescape.payment.exception.PaymentErrorCode;

@Repository
public class PaymentDao {

    private static final RowMapper<Payment> PAYMENT_ROW_MAPPER = (resultSet, rowNum) -> new Payment(
            resultSet.getLong("id"),
            resultSet.getLong("reservation_id"),
            resultSet.getString("order_id"),
            resultSet.getString("payment_key"),
            resultSet.getString("idempotency_key"),
            resultSet.getLong("amount"),
            PaymentState.valueOf(resultSet.getString("status")),
            resultSet.getTimestamp("created_at").toLocalDateTime(),
            resultSet.getTimestamp("updated_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    public PaymentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Payment save(Payment payment) {
        String sql = """
                INSERT INTO payment (reservation_id, order_id, payment_key, idempotency_key, amount, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, payment.getReservationId());
            ps.setString(2, payment.getOrderId());
            ps.setString(3, payment.getPaymentKey());
            ps.setString(4, payment.getIdempotencyKey());
            ps.setLong(5, payment.getAmount());
            ps.setString(6, payment.getState().name());
            ps.setTimestamp(7, valueOf(payment.getCreatedAt()));
            ps.setTimestamp(8, valueOf(payment.getUpdatedAt()));
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return new Payment(id, payment.getReservationId(), payment.getOrderId(), payment.getPaymentKey(), payment.getIdempotencyKey(), payment.getAmount(), payment.getState(), payment.getCreatedAt(), payment.getUpdatedAt());
    }

    public Optional<Payment> findByOrderId(String orderId) {
        String sql = """
                SELECT id, reservation_id, order_id, payment_key, idempotency_key, amount, status, created_at, updated_at
                FROM payment
                WHERE order_id = ?
                """;

        return jdbcTemplate.query(sql, PAYMENT_ROW_MAPPER, orderId).stream().findFirst();
    }

    public Optional<Payment> findByReservationId(long reservationId) {
        String sql = """
                SELECT id, reservation_id, order_id, payment_key, idempotency_key, amount, status, created_at, updated_at
                FROM payment
                WHERE reservation_id = ?
                """;

        return jdbcTemplate.query(sql, PAYMENT_ROW_MAPPER, reservationId).stream().findFirst();
    }

    public void update(Payment payment) {
        String sql = """
                UPDATE payment
                SET reservation_id = ?, order_id = ?, payment_key = ?, idempotency_key = ?, amount = ?, status = ?, created_at = ?, updated_at = ?
                WHERE id = ?
                """;
        int affected = jdbcTemplate.update(
                sql,
                payment.getReservationId(),
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getIdempotencyKey(),
                payment.getAmount(),
                payment.getState().name(),
                valueOf(payment.getCreatedAt()),
                valueOf(payment.getUpdatedAt()),
                payment.getId()
        );
        if (affected == 0) {
            throw new NotFoundException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    public void deleteByOrderId(String orderId) {
        int affected = jdbcTemplate.update("DELETE FROM payment WHERE order_id = ?", orderId);
        if (affected == 0) {
            throw new NotFoundException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    public void deleteByReservationId(long reservationId) {
        jdbcTemplate.update("DELETE FROM payment WHERE reservation_id = ?", reservationId);
    }
}
