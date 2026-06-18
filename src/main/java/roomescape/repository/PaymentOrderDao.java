package roomescape.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.payment.PaymentOrder;
import roomescape.payment.PaymentOrderStatus;
import roomescape.service.dto.PaymentOrderHistory;

@Repository
public class PaymentOrderDao {

    private static final RowMapper<PaymentOrder> paymentOrderRowMapper = (rs, rowNum) -> new PaymentOrder(
            rs.getLong("id"),
            rs.getString("order_id"),
            rs.getLong("user_id"),
            rs.getLong("schedule_id"),
            rs.getInt("amount"),
            rs.getString("idempotency_key"),
            PaymentOrderStatus.valueOf(rs.getString("status")),
            rs.getString("payment_key"),
            rs.getObject("reservation_id", Long.class),
            rs.getString("failure_code"),
            rs.getString("failure_message"),
            rs.getObject("created_at", LocalDateTime.class),
            rs.getObject("updated_at", LocalDateTime.class)
    );

    private static final RowMapper<PaymentOrderHistory> paymentOrderHistoryRowMapper = (rs, rowNum) -> new PaymentOrderHistory(
            rs.getString("order_id"),
            rs.getObject("reservation_id", Long.class),
            PaymentOrderStatus.valueOf(rs.getString("status")),
            rs.getString("payment_key"),
            rs.getInt("amount"),
            rs.getString("failure_code"),
            rs.getString("failure_message"),
            rs.getDate("schedule_date").toLocalDate(),
            rs.getTime("time_start_at").toLocalTime(),
            rs.getString("theme_name"),
            rs.getString("theme_description"),
            rs.getString("theme_thumbnail_url")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public PaymentOrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(PaymentOrder order) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "order_id", order.getOrderId(),
                "user_id", order.getMemberId(),
                "schedule_id", order.getScheduleId(),
                "amount", order.getAmount(),
                "idempotency_key", order.getIdempotencyKey(),
                "status", order.getStatus().name(),
                "created_at", order.getCreatedAt(),
                "updated_at", order.getUpdatedAt()
        )).longValue();
    }

    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = """
                SELECT id,
                       order_id,
                       user_id,
                       schedule_id,
                       amount,
                       idempotency_key,
                       status,
                       payment_key,
                       reservation_id,
                       failure_code,
                       failure_message,
                       created_at,
                       updated_at
                FROM payment_order
                WHERE order_id = ?
                """;
        List<PaymentOrder> results = jdbcTemplate.query(sql, paymentOrderRowMapper, orderId);
        return results.stream().findFirst();
    }

    public Optional<PaymentOrder> findReusableByMemberIdAndScheduleId(Long memberId, Long scheduleId) {
        String sql = """
                SELECT id,
                       order_id,
                       user_id,
                       schedule_id,
                       amount,
                       idempotency_key,
                       status,
                       payment_key,
                       reservation_id,
                       failure_code,
                       failure_message,
                       created_at,
                       updated_at
                FROM payment_order
                WHERE user_id = ?
                  AND schedule_id = ?
                  AND status IN (?, ?)
                ORDER BY created_at DESC, id DESC
                LIMIT 1
                """;
        List<PaymentOrder> results = jdbcTemplate.query(
                sql,
                paymentOrderRowMapper,
                memberId,
                scheduleId,
                PaymentOrderStatus.PENDING.name(),
                PaymentOrderStatus.CONFIRM_UNKNOWN.name()
        );
        return results.stream().findFirst();
    }

    public List<PaymentOrderHistory> findHistoriesByMemberId(Long memberId) {
        String sql = """
                SELECT po.order_id,
                       po.reservation_id,
                       po.status,
                       po.payment_key,
                       po.amount,
                       po.failure_code,
                       po.failure_message,
                       s.date AS schedule_date,
                       rt.start_at AS time_start_at,
                       th.name AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail_url AS theme_thumbnail_url
                FROM payment_order AS po
                INNER JOIN schedule AS s ON po.schedule_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS th ON s.theme_id = th.id
                WHERE po.user_id = ?
                ORDER BY po.created_at DESC, po.id DESC
                """;
        return jdbcTemplate.query(sql, paymentOrderHistoryRowMapper, memberId);
    }

    public void confirm(String orderId, String paymentKey, Long reservationId, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                """
                UPDATE payment_order
                SET status = ?,
                    payment_key = ?,
                    reservation_id = ?,
                    failure_code = NULL,
                    failure_message = NULL,
                    updated_at = ?
                WHERE order_id = ?
                """,
                PaymentOrderStatus.CONFIRMED.name(),
                paymentKey,
                reservationId,
                updatedAt,
                orderId
        );
    }

    public void markConfirmUnknown(String orderId, String code, String message, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                """
                UPDATE payment_order
                SET status = ?,
                    failure_code = ?,
                    failure_message = ?,
                    updated_at = ?
                WHERE order_id = ?
                  AND status IN (?, ?)
                """,
                PaymentOrderStatus.CONFIRM_UNKNOWN.name(),
                code,
                message,
                updatedAt,
                orderId,
                PaymentOrderStatus.PENDING.name(),
                PaymentOrderStatus.CONFIRM_UNKNOWN.name()
        );
    }

    public void fail(String orderId, String code, String message, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                """
                UPDATE payment_order
                SET status = ?,
                    failure_code = ?,
                    failure_message = ?,
                    updated_at = ?
                WHERE order_id = ?
                  AND status = ?
                """,
                PaymentOrderStatus.FAILED.name(),
                code,
                message,
                updatedAt,
                orderId,
                PaymentOrderStatus.PENDING.name()
        );
    }
}
