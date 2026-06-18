package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Payment;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public Payment save(final Payment newPayment) {
        final long newPaymentId = insertPayment(newPayment);

        return newPayment.withId(newPaymentId);
    }

    private long insertPayment(final Payment payment) {
        final String sql = """
                INSERT INTO payment (order_id, reservation_id, amount)
                VALUES (?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            preparedStatement.setString(1, payment.getOrderId());
            preparedStatement.setLong(2, payment.getReservationId());
            preparedStatement.setLong(3, payment.getAmount());

            return preparedStatement;
        }, keyHolder);

        return JdbcUtil.extractGeneratedKey(keyHolder);
    }
}
