package roomescape.payment.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.OrderRepository;
import roomescape.payment.domain.PaymentStatus;

@Repository
@RequiredArgsConstructor
public class JdbcOrderRepository implements OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Order> rowMapper = (resultSet, rowNum) -> {
        return new Order(
                resultSet.getString("id"),
                resultSet.getLong("amount"),
                resultSet.getLong("reservation_id"),
                PaymentStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getString("payment_key")
        );
    };

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO `order` (id, amount, reservation_id, status, created_at) "
                + "VALUES(:orderId, :amount, :reservationId, :status, :createdAt)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", order.getOrderId())
                .addValue("amount", order.getAmount())
                .addValue("reservationId", order.getReservationId())
                .addValue("status", order.getStatus().name())
                .addValue("createdAt", order.getCreatedAt());
        jdbcTemplate.update(sql, params);
        return order;
    }

    @Override
    public int update(Order order) {
        String sql = "UPDATE `order` "
                + "SET status = :status, created_at = :createdAt, payment_key = :paymentKey "
                + "WHERE id=:orderId";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", order.getOrderId())
                .addValue("createdAt", order.getCreatedAt())
                .addValue("status", order.getStatus().name())
                .addValue("paymentKey", order.getPaymentKey());
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public List<Order> findAllByName(String name) {
        String sql = "SELECT id, amount, reservation_id, status, created_at, payment_key "
                + "FROM `order` "
                + "WHERE reservation_id IN ("
                + "  SELECT id FROM reservation WHERE name = :name "
                + "  UNION "
                + "  SELECT id FROM pending WHERE name = :name"
                + ")";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        String sql = "SELECT id, amount, reservation_id, status, created_at, payment_key "
                + "FROM `order` WHERE id = :orderId";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId);
        List<Order> results = jdbcTemplate.query(sql, params, rowMapper);
        return results.stream().findFirst();
    }

    @Override
    public Optional<Order> findByReservationId(Long reservationId) {
        String sql = "SELECT id, amount, reservation_id, status, created_at, payment_key "
                + "FROM `order` WHERE reservation_id = :reservationId";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId);
        List<Order> results = jdbcTemplate.query(sql, params, rowMapper);
        return results.stream().findFirst();
    }

    @Override
    public boolean existsByReservationId(Long reservationId) {
        String sql = "SELECT count(1) FROM `order` WHERE reservation_id = :reservationId";
        SqlParameterSource params = new MapSqlParameterSource("reservationId", reservationId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<Order> findPendingOrdersBefore(LocalDateTime thresholdTime) {
        String sql = "SELECT id, amount, reservation_id, status, created_at, payment_key "
                + "FROM `order` "
                + "WHERE status = 'PENDING' AND created_at <= :thresholdTime";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("thresholdTime", thresholdTime);
        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
