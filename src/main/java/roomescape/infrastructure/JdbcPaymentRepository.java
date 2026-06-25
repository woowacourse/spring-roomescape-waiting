package roomescape.infrastructure;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Payment;
import roomescape.domain.repository.PaymentRepository;

@Repository
public class JdbcPaymentRepository implements PaymentRepository {
    private final SimpleJdbcInsert paymentInsert;

    public JdbcPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.paymentInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("payment")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public void save(Payment payment) {
        paymentInsert.execute(Map.of(
                "payment_order_id", payment.getPaymentOrderId(),
                "payment_key", payment.getPaymentKey(),
                "amount", payment.getAmount()
        ));
    }
}
