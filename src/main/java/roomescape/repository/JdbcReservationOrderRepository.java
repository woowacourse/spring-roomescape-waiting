package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.domain.reservationOrder.ReservationOrderRepository;

@Repository
public class JdbcReservationOrderRepository implements ReservationOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ReservationOrder> orderRowMapper = (resultSet, rowNum) -> ReservationOrder.restore(
            resultSet.getString("id"),
            resultSet.getLong("amount"),
            resultSet.getString("payment_key"),
            resultSet.getLong("reservation_id")
    );

    @Override
    public String insert(ReservationOrder order) {
        String sql = "insert into reservation_order(id, amount, reservation_id) values(?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, order.getId());
            ps.setLong(2, order.getAmount());
            ps.setLong(3, order.getReservationId());
            return ps;
        });

        return order.getId();
    }

    @Override
    public Optional<ReservationOrder> findById(String id) {
        String sql = "select id, amount, payment_key, reservation_id from reservation_order where id = ?";
        return jdbcTemplate.query(sql, orderRowMapper, id).stream()
                .findFirst();
    }

    @Override
    public void updatePaymentKey(String id, String paymentKey) {
        String sql = "update reservation_order set payment_key = ? where id = ?";
        jdbcTemplate.update(sql, paymentKey, id);
    }
}
