package roomescape.payment.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.payment.Payment;
import roomescape.payment.PaymentStatus;

@Repository
public class PaymentDao {

    private static final RowMapper<Payment> rowMapper = (rs, rowNum) ->
            new Payment(
                    rs.getLong("id"),
                    rs.getLong("reservation_id"),
                    rs.getString("payment_key"),
                    rs.getString("order_id"),
                    PaymentStatus.from(rs.getString("status")),
                    rs.getLong("amount")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public PaymentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment")
                .usingGeneratedKeyColumns("id");
    }

    public Payment save(Payment payment) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("reservation_id", payment.getReservationId())
                .addValue("payment_key", payment.getPaymentKey())
                .addValue("order_id", payment.getOrderId())
                .addValue("status", payment.getStatus().name())
                .addValue("amount", payment.getAmount());

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return new Payment(id, payment.getReservationId(), payment.getPaymentKey(), payment.getOrderId(),
                payment.getStatus(), payment.getAmount());
    }

    public Payment selectByOrderId(String orderId) {
        String sql = """
                select * from payment where order_id = ?;
                """;
        return jdbcTemplate.query(sql, rowMapper, orderId).stream()
                .findFirst()
                .orElse(null);
    }

    public Payment selectByReservationId(Long reservationId) {
        String sql = """
                select * from payment where reservation_id = ? order by id desc limit 1;
                """;
        return jdbcTemplate.query(sql, rowMapper, reservationId).stream()
                .findFirst()
                .orElse(null);
    }

    public void updateApproved(String orderId, String paymentKey, PaymentStatus status) {
        String sql = """
                update payment
                set payment_key = ?, status = ?
                where order_id = ?
                """;
        jdbcTemplate.update(sql, paymentKey, status.name(), orderId);
    }

}
