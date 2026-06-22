package roomescape.payment.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.RoomEscapeException;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderDetail;
import roomescape.payment.domain.PaymentOrderRepository;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.reservation.application.exception.ReservationErrorCode;

@RequiredArgsConstructor
@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public PaymentOrder savePending(
            String name,
            LocalDate date,
            Long themeId,
            Long timeId,
            String orderId,
            long amount,
            String idempotencyKey
    ) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns(
                        "name",
                        "date",
                        "theme_id",
                        "time_id",
                        "status",
                        "order_id",
                        "amount",
                        "idempotency_key"
                )
                .usingGeneratedKeyColumns("id");

        try {
            long id = insert.executeAndReturnKey(Map.of(
                    "name", name,
                    "date", date,
                    "theme_id", themeId,
                    "time_id", timeId,
                    "status", PaymentOrderStatus.PAYMENT_PENDING.name(),
                    "order_id", orderId,
                    "amount", amount,
                    "idempotency_key", idempotencyKey
            )).longValue();
            return new PaymentOrder(
                    id,
                    orderId,
                    amount,
                    PaymentOrderStatus.PAYMENT_PENDING,
                    null,
                    idempotencyKey
            );
        } catch (DuplicateKeyException exception) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        return jdbcTemplate.query(
                """
                        SELECT id, order_id, amount, status, payment_key, idempotency_key
                        FROM reservation
                        WHERE order_id = ?
                        """,
                (rs, rowNum) -> new PaymentOrder(
                        rs.getLong("id"),
                        rs.getString("order_id"),
                        rs.getLong("amount"),
                        PaymentOrderStatus.valueOf(rs.getString("status")),
                        rs.getString("payment_key"),
                        rs.getString("idempotency_key")
                ),
                orderId
        ).stream().findFirst();
    }

    @Override
    public List<PaymentOrderDetail> findAllByName(String name) {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, t.name AS theme_name, rt.start_at,
                               r.order_id, r.amount, r.status, r.payment_key
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
                        WHERE r.name = ? AND r.order_id IS NOT NULL
                        ORDER BY r.date ASC, rt.start_at ASC
                        """,
                (rs, rowNum) -> new PaymentOrderDetail(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("theme_name"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getString("order_id"),
                        rs.getLong("amount"),
                        PaymentOrderStatus.valueOf(rs.getString("status")),
                        rs.getString("payment_key")
                ),
                name
        );
    }

    @Override
    public void confirm(String orderId, String paymentKey) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE reservation
                        SET status = 'CONFIRMED', payment_key = ?
                        WHERE order_id = ? AND status IN ('PAYMENT_PENDING', 'CONFIRMATION_UNKNOWN')
                        """,
                paymentKey,
                orderId
        );
        if (updated == 0) {
            throw new PaymentException(PaymentErrorCode.ALREADY_PROCESSED);
        }
    }

    @Override
    public void markConfirmationUnknown(String orderId, String paymentKey) {
        jdbcTemplate.update(
                """
                        UPDATE reservation
                        SET status = 'CONFIRMATION_UNKNOWN',
                            payment_key = COALESCE(?, payment_key)
                        WHERE order_id = ? AND status IN ('PAYMENT_PENDING', 'CONFIRMATION_UNKNOWN')
                        """,
                paymentKey,
                orderId
        );
    }

    @Override
    public void keepPendingWithPaymentKey(String orderId, String paymentKey) {
        jdbcTemplate.update(
                """
                        UPDATE reservation
                        SET payment_key = COALESCE(?, payment_key)
                        WHERE order_id = ? AND status = 'PAYMENT_PENDING'
                        """,
                paymentKey,
                orderId
        );
    }

    @Override
    public void markFailed(String orderId) {
        jdbcTemplate.update(
                """
                        UPDATE reservation
                        SET status = 'PAYMENT_FAILED'
                        WHERE order_id = ? AND status IN ('PAYMENT_PENDING', 'CONFIRMATION_UNKNOWN')
                        """,
                orderId
        );
    }
}
