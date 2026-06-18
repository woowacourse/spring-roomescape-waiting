package roomescape.reservation.infra;

import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.domain.OrderId;
import roomescape.reservation.domain.PaymentAmount;
import roomescape.reservation.domain.PaymentOrder;
import roomescape.reservation.domain.PaymentOrderStatus;
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

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        return jdbcTemplate.query("""
                            SELECT id, reservation_id, order_id, amount, payment_key, status
                            FROM payment_order
                            WHERE order_id = ?
                        """,
                (rs, rowNum) -> PaymentOrder.builder()
                        .id(rs.getLong("id"))
                        .reservationId(rs.getLong("reservation_id"))
                        .orderId(OrderId.builder()
                                .value(rs.getString("order_id"))
                                .build())
                        .amount(PaymentAmount.builder()
                                .value(rs.getLong("amount"))
                                .build())
                        .paymentKey(rs.getString("payment_key"))
                        .status(PaymentOrderStatus.valueOf(rs.getString("status")))
                        .build(),
                orderId).stream().findFirst();
    }

    @Override
    public Integer confirm(PaymentOrder paymentOrder) {
        try {
            return jdbcTemplate.update(
                    """
                            UPDATE payment_order
                            SET payment_key = ?, status = ?
                            WHERE order_id = ?
                                AND status = ?
                                AND payment_key IS NULL
                            """,
                    paymentOrder.getPaymentKey(),
                    paymentOrder.getStatus().name(),
                    paymentOrder.getOrderId().value(),
                    PaymentOrderStatus.PENDING.name()
            );
        } catch (DuplicateKeyException e) {
            throw new UniqueConstraintViolationException(e);
        }
    }

    @Override
    public Integer deletePendingByOrderId(String orderId) {
        return jdbcTemplate.update(
                """
                        DELETE FROM payment_order
                        WHERE order_id = ?
                            AND status = ?
                            AND payment_key IS NULL
                        """,
                orderId,
                PaymentOrderStatus.PENDING.name()
        );
    }

    @Override
    public Integer deleteByReservationId(Long reservationId) {
        return jdbcTemplate.update(
                "DELETE FROM payment_order WHERE reservation_id = ?",
                reservationId
        );
    }
}
