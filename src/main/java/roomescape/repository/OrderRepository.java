package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Order;
import roomescape.domain.OrderStatus;
import roomescape.domain.Reservation;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Order save(final Order order) {
        final String sql = """
                INSERT INTO orders (order_id, amount, reservation_id, status)
                VALUES (:orderId, :amount, :reservationId, :status)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("orderId", order.getOrderId())
                .addValue("amount", order.getAmount())
                .addValue("reservationId", order.getReservationId())
                .addValue("status", order.getStatus().name());

        try {
            jdbcTemplate.update(sql, param, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_RESERVED);
        }

        final long newOrderId = keyHolder.getKey().longValue();
        return order.withId(newOrderId);
    }

    public Optional<Order> findByReservationId(final Long reservationId) {
        final String sql = """
                SELECT id, order_id, amount, payment_key, reservation_id, status
                FROM orders
                WHERE reservation_id = :reservationId;
                """;

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("reservationId", reservationId);

            final Order order = jdbcTemplate.queryForObject(sql, param, orderRowMapper());
            return Optional.of(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Order> findByOrderId(final String orderId) {
        final String sql = """
                SELECT id, order_id, amount, payment_key, reservation_id, status
                FROM orders
                WHERE order_id = :orderId
                """;
        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("orderId", orderId);
            final Order order = jdbcTemplate.queryForObject(sql, param, orderRowMapper());
            return Optional.of(order);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void confirm(final Long id, final String paymentKey) {
        final String sql = """
                UPDATE orders
                SET payment_key = :paymentKey, status = 'COMPLETED'
                WHERE id = :id
                """;
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("paymentKey", paymentKey)
                .addValue("id", id);
        jdbcTemplate.update(sql, param);
    }

    private RowMapper<Order> orderRowMapper() {
        return (rs, rowNum) -> Order.createWithId(
                rs.getLong("id"),
                rs.getString("order_id"),
                rs.getLong("amount"),
                rs.getString("payment_key"),
                rs.getLong("reservation_id"),
                OrderStatus.valueOf(rs.getString("status"))
        );
    }
}
