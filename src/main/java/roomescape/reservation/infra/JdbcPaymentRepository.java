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
import roomescape.reservation.domain.Payment;
import roomescape.reservation.domain.PaymentAmount;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservation.domain.repository.PaymentRepository;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Payment save(Payment payment) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservation_id", payment.getReservationId())
                .addValue("order_id", payment.getOrderId().value())
                .addValue("amount", payment.getAmount().value())
                .addValue("status", payment.getStatus().name());
        try {
            Long id = jdbcInsert.executeAndReturnKey(params).longValue();
            return payment.withId(id);
        } catch (DuplicateKeyException e) {
            throw new UniqueConstraintViolationException(e);
        }
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return jdbcTemplate.query("""
                            SELECT id, reservation_id, order_id, amount, payment_key, status
                            FROM payment
                            WHERE order_id = ?
                        """,
                (rs, rowNum) -> Payment.builder()
                        .id(rs.getLong("id"))
                        .reservationId(rs.getLong("reservation_id"))
                        .orderId(OrderId.builder()
                                .value(rs.getString("order_id"))
                                .build())
                        .amount(PaymentAmount.builder()
                                .value(rs.getLong("amount"))
                                .build())
                        .paymentKey(rs.getString("payment_key"))
                        .status(PaymentStatus.valueOf(rs.getString("status")))
                        .build(),
                orderId).stream().findFirst();
    }

    @Override
    public Integer confirm(Payment payment) {
        try {
            return jdbcTemplate.update(
                    """
                            UPDATE payment
                            SET payment_key = ?, status = ?
                            WHERE order_id = ?
                                AND status = ?
                                AND payment_key IS NULL
                            """,
                    payment.getPaymentKey(),
                    payment.getStatus().name(),
                    payment.getOrderId().value(),
                    PaymentStatus.PENDING.name()
            );
        } catch (DuplicateKeyException e) {
            throw new UniqueConstraintViolationException(e);
        }
    }

    @Override
    public Integer deletePendingByOrderId(String orderId) {
        return jdbcTemplate.update(
                """
                        DELETE FROM payment
                        WHERE order_id = ?
                            AND status = ?
                            AND payment_key IS NULL
                        """,
                orderId,
                PaymentStatus.PENDING.name()
        );
    }

    @Override
    public Integer deleteByReservationId(Long reservationId) {
        return jdbcTemplate.update(
                "DELETE FROM payment WHERE reservation_id = ?",
                reservationId
        );
    }
}
