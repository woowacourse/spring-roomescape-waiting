package roomescape.payment.infra;

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
import roomescape.payment.domain.OrderStatus;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.TimeSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcOrderRepository implements OrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Order> rowMapper = (resultSet, rowNum) -> {
        Theme theme = Theme.builder()
                .id(resultSet.getLong("t_id"))
                .name(resultSet.getString("t_name"))
                .thumbnailImageUrl(resultSet.getString("t_thumbnail_image_url"))
                .description(resultSet.getString("t_description"))
                .durationTime(resultSet.getTime("t_duration_time").toLocalTime())
                .build();

        ReservationTime time = ReservationTime.builder()
                .id(resultSet.getLong("rt_id"))
                .startAt(resultSet.getTime("rt_start_at").toLocalTime())
                .build();

        TimeSlot slot = TimeSlot.builder()
                .id(resultSet.getLong("ts_id"))
                .date(resultSet.getDate("ts_date").toLocalDate())
                .time(time)
                .theme(theme)
                .build();

        ActiveReservation reservation = ActiveReservation.builder()
                .id(resultSet.getLong("r_id"))
                .name(resultSet.getString("r_name"))
                .slot(slot)
                .is_deleted(resultSet.getLong("r_is_deleted"))
                .createdAt(resultSet.getTimestamp("r_created_at").toLocalDateTime())
                .build();

        // Order는 @AllArgsConstructor가 적용되어 있으므로 생성자 사용
        return new Order(
                resultSet.getString("o_id"),
                resultSet.getLong("o_amount"),
                reservation,
                OrderStatus.valueOf(resultSet.getString("o_status")),
                resultSet.getTimestamp("o_created_at").toLocalDateTime(),
                resultSet.getString("o_payment_key")
        );
    };

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO `order` (id, amount, reservation_id, status, created_at) "
                + "VALUES(:orderId, :amount, :reservationId, :status, :createdAt)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", order.getOrderId())
                .addValue("amount", order.getAmount())
                .addValue("reservationId", order.getReservation().getId())
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
        String sql = "SELECT "
                + "o.id AS o_id, o.amount AS o_amount, o.status AS o_status, o.created_at AS o_created_at, o.payment_key AS o_payment_key, "
                + "r.id AS r_id, r.name AS r_name, r.is_deleted AS r_is_deleted, r.created_at AS r_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM `order` o "
                + "INNER JOIN reservation r ON o.reservation_id = r.id "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE r.name = :name";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        String sql = "SELECT "
                + "o.id AS o_id, o.amount AS o_amount, o.status AS o_status, o.created_at AS o_created_at, o.payment_key AS o_payment_key, "
                + "r.id AS r_id, r.name AS r_name, r.is_deleted AS r_is_deleted, r.created_at AS r_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM `order` o "
                + "INNER JOIN reservation r ON o.reservation_id = r.id "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE o.id = :orderId";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("orderId", orderId);

        List<Order> results = jdbcTemplate.query(sql, params, rowMapper);

        return results.stream().findFirst();
    }

    @Override
    public Optional<Order> findByReservationId(Long reservationId) {
        String sql = "SELECT "
                + "o.id AS o_id, o.amount AS o_amount, o.status AS o_status, o.created_at AS o_created_at, o.payment_key AS o_payment_key, "
                + "r.id AS r_id, r.name AS r_name, r.is_deleted AS r_is_deleted, r.created_at AS r_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM `order` o "
                + "INNER JOIN reservation r ON o.reservation_id = r.id "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE r.id = :reservationId";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId);
        List<Order> results = jdbcTemplate.query(sql, params, rowMapper);
        return results.stream().findFirst();
    }
}
