package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.OrderId;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public Payment save(final Payment newPayment) {
        final long newPaymentId = insertPayment(newPayment);

        return newPayment.withId(newPaymentId);
    }

    private long insertPayment(final Payment payment) {
        final String sql = """
                INSERT INTO payment (order_id, reservation_id, amount)
                VALUES (?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setString(1, payment.getOrderId().id());
            preparedStatement.setLong(2, payment.getReservationId());
            preparedStatement.setLong(3, payment.getAmount());

            return preparedStatement;
        }, keyHolder);

        return JdbcUtil.extractGeneratedKey(keyHolder);
    }

    public void update(final Payment payment) {
        final String sql = """
                UPDATE payment
                SET payment_key = ?, status = ?
                WHERE order_id = ?
                """;

        jdbcTemplate.update(
                sql,
                payment.getPaymentKey(),
                payment.getStatus().name(),
                payment.getOrderId().id()
        );
    }

    public boolean deleteByOrderId(final OrderId orderId) {
        final String sql = """
                DELETE FROM payment
                WHERE order_id = ?
                """;

        return jdbcTemplate.update(sql, orderId.id()) > 0;
    }

    public Optional<Payment> findByOrderId(final OrderId orderId) {
        final String sql = """
                SELECT id, order_id, reservation_id, amount, payment_key, status
                FROM payment
                WHERE order_id = ?
                """;

        try {
            final Payment payment = jdbcTemplate.queryForObject(
                    sql,
                    this::mapToDomain,
                    orderId.id()
            );

            return Optional.of(payment);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * ResultSet - Domain 매핑 메서드
     */
    private Payment mapToDomain(final ResultSet resultSet, final int rowNum) throws SQLException {
        return Payment.from(
                resultSet.getLong("id"),
                new OrderId(resultSet.getString("order_id")),
                resultSet.getLong("reservation_id"),
                resultSet.getLong("amount"),
                resultSet.getString("payment_key"),
                PaymentStatus.from(resultSet.getString("status"))
        );
    }
}
