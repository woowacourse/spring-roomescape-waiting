package roomescape.payment.infra;

import java.time.LocalDate;
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
            long amount
    ) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns("name", "date", "theme_id", "time_id", "status", "order_id", "amount")
                .usingGeneratedKeyColumns("id");

        try {
            long id = insert.executeAndReturnKey(Map.of(
                    "name", name,
                    "date", date,
                    "theme_id", themeId,
                    "time_id", timeId,
                    "status", PaymentOrderStatus.PAYMENT_PENDING.name(),
                    "order_id", orderId,
                    "amount", amount
            )).longValue();
            return new PaymentOrder(id, orderId, amount, PaymentOrderStatus.PAYMENT_PENDING, null);
        } catch (DuplicateKeyException exception) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        return jdbcTemplate.query(
                """
                        SELECT id, order_id, amount, status, payment_key
                        FROM reservation
                        WHERE order_id = ?
                        """,
                (rs, rowNum) -> new PaymentOrder(
                        rs.getLong("id"),
                        rs.getString("order_id"),
                        rs.getLong("amount"),
                        PaymentOrderStatus.valueOf(rs.getString("status")),
                        rs.getString("payment_key")
                ),
                orderId
        ).stream().findFirst();
    }

    @Override
    public void confirm(String orderId, String paymentKey) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE reservation
                        SET status = 'CONFIRMED', payment_key = ?
                        WHERE order_id = ? AND status = 'PAYMENT_PENDING'
                        """,
                paymentKey,
                orderId
        );
        if (updated == 0) {
            throw new PaymentException(PaymentErrorCode.ALREADY_PROCESSED);
        }
    }

    @Override
    public void deletePending(String orderId) {
        jdbcTemplate.update(
                "DELETE FROM reservation WHERE order_id = ? AND status = 'PAYMENT_PENDING'",
                orderId
        );
    }
}
