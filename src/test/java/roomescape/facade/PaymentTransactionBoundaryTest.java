package roomescape.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import roomescape.config.TestClockConfig;
import roomescape.domain.ReservationStatus;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:payment_transaction_boundary;MODE=MySQL;DB_CLOSE_DELAY=-1")
@Import({TestClockConfig.class, PaymentTransactionBoundaryTest.TransactionRecordingPaymentGatewayConfig.class})
class PaymentTransactionBoundaryTest {

    @Autowired
    private ReceptionFacade receptionFacade;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionRecordingPaymentGateway paymentGateway;

    @BeforeEach
    void beforeEach() {
        paymentGateway.reset();
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail_url) VALUES (?, ?, ?)",
                "방탈출1", "방탈출1 설명", "theme/url.png");
    }

    @AfterEach
    void afterEach() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'";
        List<String> tableNames = jdbcTemplate.queryForList(sql, String.class);

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1");
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void confirmPaymentCallsPaymentGatewayOutsideTransactionAndConfirmsReservationTest() {
        savePendingReservation("order_success", "idempotency_success", 1L);

        receptionFacade.confirmPayment("payment_key", "order_success", 50000L);

        assertThat(paymentGateway.transactionActiveDuringConfirm()).containsExactly(false);
        assertThat(findReservationStatus("order_success")).isEqualTo(ReservationStatus.CONFIRMED.name());
        assertThat(findPaymentKey("order_success")).isEqualTo("payment_key");
    }

    @Test
    void unknownPaymentCallsPaymentGatewayOutsideTransactionPersistsUnknownAndThrowsTest() {
        savePendingReservation("order_unknown", "idempotency_unknown", 1L);
        paymentGateway.failUnknown();

        assertThatThrownBy(() -> receptionFacade.confirmPayment("payment_key", "order_unknown", 50000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_UNKNOWN));

        assertThat(paymentGateway.transactionActiveDuringConfirm()).containsExactly(false, false, false);
        assertThat(findReservationStatus("order_unknown")).isEqualTo(ReservationStatus.PAYMENT_UNKNOWN.name());
    }

    private void savePendingReservation(String orderId, String idempotencyKey, Long timeId) {
        String sql = """
                INSERT INTO reservation(name, date, time_id, theme_id, status, order_id, idempotency_key, amount)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, "예약자", "2026-05-02", timeId, 1L, ReservationStatus.PENDING.name(),
                orderId, idempotencyKey, 50000L);
    }

    private String findReservationStatus(String orderId) {
        return jdbcTemplate.queryForObject("SELECT status FROM reservation WHERE order_id = ?", String.class, orderId);
    }

    private String findPaymentKey(String orderId) {
        return jdbcTemplate.queryForObject("SELECT payment_key FROM reservation WHERE order_id = ?", String.class,
                orderId);
    }

    @TestConfiguration
    static class TransactionRecordingPaymentGatewayConfig {

        @Bean
        @Primary
        TransactionRecordingPaymentGateway transactionRecordingPaymentGateway() {
            return new TransactionRecordingPaymentGateway();
        }
    }

    static class TransactionRecordingPaymentGateway implements PaymentGateway {

        private final List<Boolean> transactionActiveDuringConfirm = new ArrayList<>();
        private boolean failUnknown;

        @Override
        public PaymentResult confirm(PaymentConfirmation confirmation) {
            transactionActiveDuringConfirm.add(TransactionSynchronizationManager.isActualTransactionActive());
            if (failUnknown) {
                throw new RoomEscapeException(DomainErrorCode.PAYMENT_UNKNOWN);
            }
            return new PaymentResult(confirmation.paymentKey(), confirmation.orderId(), "DONE", confirmation.amount());
        }

        void failUnknown() {
            failUnknown = true;
        }

        void reset() {
            transactionActiveDuringConfirm.clear();
            failUnknown = false;
        }

        List<Boolean> transactionActiveDuringConfirm() {
            return transactionActiveDuringConfirm;
        }
    }
}
