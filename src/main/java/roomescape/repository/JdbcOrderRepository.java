package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.order.Order;
import roomescape.domain.order.OrderRepository;

@Repository
public class JdbcOrderRepository implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Order> orderRowMapper = (resultSet, rowNum) -> new Order(
            resultSet.getString("id"),
            resultSet.getLong("amount"),
            resultSet.getLong("reservation_id")
    );

    @Override
    public String insert(Order order) {
        String sql = "insert into order(id, amount, reservation_id) values(?, ?, ?)";

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
    public Optional<Order> findById(String id) {
        String sql = "select id, amount, reservation_id from order where id = ?";
        return jdbcTemplate.query(sql, orderRowMapper, id).stream()
                .findFirst();
    }
}
