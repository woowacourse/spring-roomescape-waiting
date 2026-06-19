package roomescape.dao.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.order.Order;

public final class OrderMapper {

    public static final RowMapper<Order> ORDER_ROW_MAPPER = (rs, rowNum) -> {
        return new Order(
                rs.getString("id"),
                rs.getLong("amount"),
                rs.getLong("reservation_id")
        );
    };

    private OrderMapper() {
    }
}
