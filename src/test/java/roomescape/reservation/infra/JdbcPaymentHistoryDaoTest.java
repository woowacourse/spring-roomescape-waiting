package roomescape.reservation.infra;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.application.dao.PaymentHistoryDao;
import roomescape.reservation.application.dto.PaymentHistoryDetail;
import roomescape.reservation.domain.Payment;
import roomescape.reservation.domain.repository.PaymentRepository;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import({JdbcPaymentHistoryDao.class, JdbcPaymentRepository.class})
class JdbcPaymentHistoryDaoTest {

    private static final String USERNAME = "스타크";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaymentHistoryDao paymentHistoryDao;

    @Autowired
    private PaymentRepository paymentRepository;

    private TestDataHelper testHelper;
    private Long themeId;
    private Long tenTimeId;
    private Long elevenTimeId;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
        themeId = testHelper.insertTheme("공포 테마", "무서운 테마", "https://example.com/theme.jpg");
        tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        elevenTimeId = testHelper.insertReservationTime(LocalTime.of(11, 0));
    }

    @DisplayName("사용자 이름으로 확정 결제와 대기 결제 내역을 조회합니다.")
    @Test
    void find_by_name() {
        Payment confirmedPayment = paymentRepository.save(Payment.create(
                insertReservation(USERNAME, LocalDate.of(2099, 12, 31), tenTimeId),
                50_000L
        ));
        testHelper.confirmPayment(confirmedPayment, "payment-key-confirmed");
        Payment pendingPayment = paymentRepository.save(Payment.create(
                insertReservation(USERNAME, LocalDate.of(2100, 1, 1), elevenTimeId),
                50_000L
        ));
        paymentRepository.save(Payment.create(
                insertReservation("비밥", LocalDate.of(2100, 1, 2), tenTimeId),
                50_000L
        ));

        List<PaymentHistoryDetail> histories = paymentHistoryDao.findByName(USERNAME);
        PaymentHistoryDetail first = histories.getFirst();
        PaymentHistoryDetail second = histories.get(1);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(histories).hasSize(2);
            softly.assertThat(first.username()).isEqualTo(USERNAME);
            softly.assertThat(first.orderId()).isEqualTo(confirmedPayment.getOrderId().value());
            softly.assertThat(first.paymentKey()).isEqualTo("payment-key-confirmed");
            softly.assertThat(first.paymentStatus()).isEqualTo("CONFIRMED");
            softly.assertThat(first.reservationStatus()).isEqualTo("CONFIRMED");
            softly.assertThat(first.themeId()).isEqualTo(themeId);
            softly.assertThat(first.timeId()).isEqualTo(tenTimeId);
            softly.assertThat(first.startAt()).isEqualTo(LocalTime.of(10, 0));
            softly.assertThat(second.username()).isEqualTo(USERNAME);
            softly.assertThat(second.orderId()).isEqualTo(pendingPayment.getOrderId().value());
            softly.assertThat(second.paymentKey()).isNull();
            softly.assertThat(second.paymentStatus()).isEqualTo("PENDING");
            softly.assertThat(second.reservationStatus()).isEqualTo("PAYMENT_PENDING");
        });
    }

    @DisplayName("결제 내역이 없으면 빈 목록을 반환합니다.")
    @Test
    void find_by_name_empty() {
        List<PaymentHistoryDetail> histories = paymentHistoryDao.findByName(USERNAME);

        SoftAssertions.assertSoftly(softly -> softly.assertThat(histories).isEmpty());
    }

    private Long insertReservation(String username, LocalDate date, Long timeId) {
        return testHelper.insertReservation(username, date, themeId, timeId);
    }
}
