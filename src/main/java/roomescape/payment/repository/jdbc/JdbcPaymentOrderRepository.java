package roomescape.payment.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.payment.repository.PaymentOrderRepository;
import roomescape.payment.repository.entity.PaymentOrderEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public PaymentOrder save(final PaymentOrder paymentOrder) {
        final PaymentOrderEntity entity = toEntity(paymentOrder);
        final long id = insert(entity);

        return PaymentOrder.of(
                id,
                paymentOrder.getOrderId(),
                paymentOrder.getAmount(),
                paymentOrder.getPaymentKey(),
                paymentOrder.getIdempotencyKey(),
                paymentOrder.getStatus(),
                paymentOrder.getReservationId()
        );
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(final String orderId) {
        final String sql = """
                SELECT id, order_id, amount, payment_key, idempotency_key, status, reservation_id
                FROM payment_order
                WHERE order_id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, this::mapToDomain, orderId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public boolean complete(final String orderId, final String paymentKey) {
        final String sql = """
                UPDATE payment_order
                SET status = ?, payment_key = ?
                WHERE order_id = ? AND status = ?
                """;

        return jdbcTemplate.update(
                sql,
                PaymentOrderStatus.COMPLETED.name(),
                paymentKey,
                orderId,
                PaymentOrderStatus.READY.name()
        ) > 0;
    }

    @Override
    public boolean deleteByOrderId(final String orderId) {
        final String sql = """
                DELETE FROM payment_order
                WHERE order_id = ?
                """;

        return jdbcTemplate.update(sql, orderId) > 0;
    }

    private long insert(final PaymentOrderEntity entity) {
        final String sql = """
                INSERT INTO payment_order (order_id, amount, payment_key, idempotency_key, status, reservation_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});

            preparedStatement.setString(1, entity.orderId());
            preparedStatement.setInt(2, entity.amount());
            preparedStatement.setString(3, entity.paymentKey());
            preparedStatement.setString(4, entity.idempotencyKey());
            preparedStatement.setString(5, entity.status());
            preparedStatement.setLong(6, entity.reservationId());

            return preparedStatement;
        }, keyHolder);

        return generatedIdFrom(keyHolder);
    }

    private long generatedIdFrom(final KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성된 결제 주문 id를 가져오지 못했습니다.");
        }

        return keyHolder.getKey().longValue();
    }

    private PaymentOrder mapToDomain(final ResultSet resultSet, final int rowNum) throws SQLException {
        return PaymentOrder.of(
                resultSet.getLong("id"),
                resultSet.getString("order_id"),
                resultSet.getInt("amount"),
                resultSet.getString("payment_key"),
                resultSet.getString("idempotency_key"),
                PaymentOrderStatus.valueOf(resultSet.getString("status")),
                resultSet.getLong("reservation_id")
        );
    }

    private PaymentOrderEntity toEntity(final PaymentOrder paymentOrder) {
        return new PaymentOrderEntity(
                paymentOrder.getId(),
                paymentOrder.getOrderId(),
                paymentOrder.getAmount(),
                paymentOrder.getPaymentKey(),
                paymentOrder.getIdempotencyKey(),
                paymentOrder.getStatus().name(),
                paymentOrder.getReservationId()
        );
    }
}
