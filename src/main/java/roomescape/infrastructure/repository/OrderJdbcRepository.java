package roomescape.infrastructure.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Order;
import roomescape.domain.OrderRepository;
import roomescape.domain.OrderStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@Repository
public class OrderJdbcRepository implements OrderRepository {

    private static final String SELECT_BY_ORDER_ID = """
            SELECT
                ro.order_id,
                ro.order_name,
                ro.amount,
                ro.payment_key,
                ro.status,
                ro.reserver_name,
                ro.reservation_date,
                rt.id AS time_id,
                rt.start_at,
                t.id AS theme_id,
                t.name AS theme_name,
                t.description AS theme_description,
                t.thumbnail_image_url AS theme_thumbnail_image_url
            FROM reservation_order AS ro
            INNER JOIN reservation_time AS rt ON ro.time_id = rt.id
            INNER JOIN theme AS t ON ro.theme_id = t.id
            WHERE ro.order_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public OrderJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail_image_url")
        );
        Reservation reservation = new Reservation(
                new Member(rs.getString("reserver_name")),
                new Slot(
                        rs.getDate("reservation_date").toLocalDate(),
                        reservationTime,
                        theme
                )
        );

        return new Order(
                rs.getString("order_id"),
                rs.getString("order_name"),
                rs.getLong("amount"),
                reservation,
                rs.getString("payment_key"),
                OrderStatus.valueOf(rs.getString("status"))
        );
    };

    @Override
    public void save(Order order) {
        String insertSql = """
                INSERT INTO reservation_order (
                    order_id,
                    order_name,
                    amount,
                    payment_key,
                    status,
                    reserver_name,
                    reservation_date,
                    time_id,
                    theme_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                insertSql,
                order.getOrderId(),
                order.getOrderName(),
                order.getAmount(),
                order.getPaymentKey(),
                order.getStatus().name(),
                order.getReservation().getReserver().name(),
                Date.valueOf(order.getReservation().getDate()),
                order.getReservation().getTime().getId(),
                order.getReservation().getTheme().getId()
        );
    }

    @Override
    public void update(Order order) {
        String sql = """
                UPDATE reservation_order
                SET order_name = ?,
                    amount = ?,
                    payment_key = ?,
                    status = ?,
                    reserver_name = ?,
                    reservation_date = ?,
                    time_id = ?,
                    theme_id = ?
                WHERE order_id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                order.getOrderName(),
                order.getAmount(),
                order.getPaymentKey(),
                order.getStatus().name(),
                order.getReservation().getReserver().name(),
                Date.valueOf(order.getReservation().getDate()),
                order.getReservation().getTime().getId(),
                order.getReservation().getTheme().getId(),
                order.getOrderId()
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("수정할 주문을 찾을 수 없습니다: " + order.getOrderId());
        }
    }

    @Override
    public Optional<Order> findById(String orderId) {
        List<Order> orders = jdbcTemplate.query(SELECT_BY_ORDER_ID, orderRowMapper, orderId);
        return orders.stream().findFirst();
    }
}
