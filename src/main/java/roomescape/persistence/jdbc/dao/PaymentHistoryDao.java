package roomescape.persistence.jdbc.dao;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.payment.PaymentHistory;
import roomescape.persistence.util.RepositoryExceptionTranslator;

@Repository
@RequiredArgsConstructor
public class PaymentHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public void save(PaymentHistory paymentHistory) {
        String sql = """
                MERGE INTO payment_history (order_id, payment_key, amount, status, created_at)
                KEY (order_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        RepositoryExceptionTranslator.execute(() ->
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setString(1, paymentHistory.getOrderId());
                    ps.setString(2, paymentHistory.getPaymentKey());
                    ps.setLong(3, paymentHistory.getAmount());
                    ps.setString(4, paymentHistory.getStatus().name());
                    ps.setTimestamp(5, Timestamp.valueOf(paymentHistory.getCreatedAt()));
                    return ps;
                }), "이미 처리된 결제입니다.");
    }
}
