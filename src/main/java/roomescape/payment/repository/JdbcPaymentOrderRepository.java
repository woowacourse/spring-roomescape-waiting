package roomescape.payment.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.InfrastructureException;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderDetails;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcPaymentOrderRepository.class);

    private final RowMapper<PaymentOrder> paymentOrderRowMapper = (resultSet, rowNum) -> new PaymentOrder(
            resultSet.getLong("id"),
            resultSet.getString("order_id"),
            resultSet.getLong("amount"),
            PaymentOrderStatus.valueOf(resultSet.getString("status")),
            resultSet.getString("name"),
            resultSet.getDate("date").toLocalDate(),
            resultSet.getLong("time_id"),
            resultSet.getLong("theme_id"),
            resultSet.getString("payment_key"),
            resultSet.getObject("reservation_id", Long.class),
            resultSet.getString("failure_code"),
            resultSet.getString("failure_message"),
            resultSet.getTimestamp("created_at").toLocalDateTime(),
            resultSet.getTimestamp("updated_at").toLocalDateTime(),
            toLocalDateTimeOrNull(resultSet.getTimestamp("confirmed_at"))
    );

    private final RowMapper<PaymentOrderDetails> paymentOrderDetailsRowMapper = (resultSet, rowNum) -> {
        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail")
        );

        return new PaymentOrderDetails(
                resultSet.getString("order_id"),
                resultSet.getLong("amount"),
                PaymentOrderStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("name"),
                resultSet.getDate("date").toLocalDate(),
                time,
                theme,
                resultSet.getObject("reservation_id", Long.class),
                resultSet.getString("failure_code"),
                resultSet.getString("failure_message"),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime(),
                toLocalDateTimeOrNull(resultSet.getTimestamp("confirmed_at"))
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcPaymentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowCount = insert(paymentOrder, keyHolder);
        validateRowCount(rowCount, paymentOrder, "생성");

        Number key = keyHolder.getKey();
        if (key == null) {
            log.error("Payment order insert did not return generated id. orderId={}", paymentOrder.getOrderId());
            throw new InfrastructureException("결제 주문 생성에 실패했습니다.");
        }

        return findByOrderId(paymentOrder.getOrderId())
                .orElseThrow(() -> new InfrastructureException("결제 주문 생성 결과를 조회하지 못했습니다."));
    }

    @Override
    public PaymentOrder update(PaymentOrder paymentOrder) {
        String sql = """
                UPDATE payment_order
                SET status = ?,
                    payment_key = ?,
                    reservation_id = ?,
                    failure_code = ?,
                    failure_message = ?,
                    updated_at = ?,
                    confirmed_at = ?
                WHERE order_id = ?
                """;

        int rowCount = jdbcTemplate.update(
                sql,
                paymentOrder.getStatus().name(),
                paymentOrder.getPaymentKey(),
                paymentOrder.getReservationId(),
                paymentOrder.getFailureCode(),
                paymentOrder.getFailureMessage(),
                Timestamp.valueOf(paymentOrder.getUpdatedAt()),
                toTimestampOrNull(paymentOrder.getConfirmedAt()),
                paymentOrder.getOrderId()
        );

        validateRowCount(rowCount, paymentOrder, "변경");

        return findByOrderId(paymentOrder.getOrderId())
                .orElseThrow(() -> new InfrastructureException("결제 주문 변경 결과를 조회하지 못했습니다."));
    }

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        String sql = """
                SELECT id,
                       order_id,
                       amount,
                       status,
                       name,
                       date,
                       time_id,
                       theme_id,
                       payment_key,
                       reservation_id,
                       failure_code,
                       failure_message,
                       created_at,
                       updated_at,
                       confirmed_at
                FROM payment_order
                WHERE order_id = ?
                """;

        return jdbcTemplate.query(sql, paymentOrderRowMapper, orderId)
                .stream()
                .findFirst();
    }

    @Override
    public List<PaymentOrderDetails> findDetailsByName(String name) {
        String sql = """
                SELECT po.order_id,
                       po.amount,
                       po.status,
                       po.name,
                       po.date,
                       po.time_id,
                       rt.start_at,
                       po.theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.thumbnail AS theme_thumbnail,
                       po.reservation_id,
                       po.failure_code,
                       po.failure_message,
                       po.created_at,
                       po.updated_at,
                       po.confirmed_at
                FROM payment_order po
                JOIN reservation_time rt ON po.time_id = rt.id
                JOIN theme t ON po.theme_id = t.id
                WHERE po.name = ?
                ORDER BY po.created_at DESC,
                         po.id DESC
                """;

        return jdbcTemplate.query(sql, paymentOrderDetailsRowMapper, name);
    }

    private int insert(PaymentOrder paymentOrder, KeyHolder keyHolder) {
        String sql = """
                INSERT INTO payment_order (
                    order_id,
                    amount,
                    status,
                    name,
                    date,
                    time_id,
                    theme_id,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, paymentOrder.getOrderId());
            preparedStatement.setLong(2, paymentOrder.getAmount());
            preparedStatement.setString(3, paymentOrder.getStatus().name());
            preparedStatement.setString(4, paymentOrder.getName());
            preparedStatement.setDate(5, Date.valueOf(paymentOrder.getDate()));
            preparedStatement.setLong(6, paymentOrder.getTimeId());
            preparedStatement.setLong(7, paymentOrder.getThemeId());
            preparedStatement.setTimestamp(8, Timestamp.valueOf(paymentOrder.getCreatedAt()));
            preparedStatement.setTimestamp(9, Timestamp.valueOf(paymentOrder.getUpdatedAt()));
            return preparedStatement;
        }, keyHolder);
    }

    private void validateRowCount(int rowCount, PaymentOrder paymentOrder, String action) {
        if (rowCount != 1) {
            log.error(
                    "Payment order {} affected unexpected row count. rowCount={}, orderId={}, status={}",
                    action,
                    rowCount,
                    paymentOrder.getOrderId(),
                    paymentOrder.getStatus()
            );
            throw new InfrastructureException("결제 주문 " + action + "에 실패했습니다.");
        }
    }

    private static java.time.LocalDateTime toLocalDateTimeOrNull(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    private static Timestamp toTimestampOrNull(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.valueOf(localDateTime);
    }
}
