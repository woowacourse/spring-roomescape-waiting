package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationOrder.OrderStatus;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.domain.reservationOrder.ReservationOrderRepository;

@Repository
public class JdbcReservationOrderRepository implements ReservationOrderRepository {

    private static final String SELECT_COLUMNS = "select id, amount, payment_key, reservation_id, status from reservation_order";

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ReservationOrder> orderRowMapper = (resultSet, rowNum) -> ReservationOrder.restore(
            resultSet.getString("id"),
            resultSet.getLong("amount"),
            resultSet.getString("payment_key"),
            resultSet.getLong("reservation_id"),
            OrderStatus.valueOf(resultSet.getString("status"))
    );

    @Override
    public String insert(ReservationOrder order) {
        String sql = "insert into reservation_order(id, amount, payment_key, reservation_id, status) values(?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, order.getId());
            ps.setLong(2, order.getAmount());
            ps.setString(3, order.getPaymentKey());
            ps.setLong(4, order.getReservationId());
            ps.setString(5, order.getStatus().name());
            return ps;
        });

        return order.getId();
    }

    @Override
    public Optional<ReservationOrder> findById(String id) {
        String sql = SELECT_COLUMNS + " where id = ?";
        return jdbcTemplate.query(sql, orderRowMapper, id).stream()
                .findFirst();
    }

    @Override
    public Optional<ReservationOrder> findByReservationId(long reservationId) {
        String sql = SELECT_COLUMNS + " where reservation_id = ?";
        return jdbcTemplate.query(sql, orderRowMapper, reservationId).stream()
                .findFirst();
    }

    @Override
    public void updatePaymentKey(String id, String paymentKey) {
        String sql = "update reservation_order set payment_key = ?, status = ? where id = ?";
        jdbcTemplate.update(sql, paymentKey, OrderStatus.CONFIRMED.name(), id);
    }

    @Override
    public void updateStatus(String id, OrderStatus status) {
        String sql = "update reservation_order set status = ? where id = ?";
        jdbcTemplate.update(sql, status.name(), id);
    }
}
