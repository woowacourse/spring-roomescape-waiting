package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.PaymentHistoryResult;
import roomescape.reservation.application.service.PaymentQueryService;
import roomescape.reservation.domain.Payment;
import roomescape.reservation.domain.repository.PaymentRepository;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;
import roomescape.theme.application.dto.ThemeResult;

@ServiceTest
@Transactional
class PaymentQueryServiceTest {

    private static final String USERNAME = "스타크";

    @Autowired
    private PaymentQueryService paymentQueryService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("사용자의 결제 내역을 예약 문맥과 함께 조회합니다.")
    @Test
    void find_by_name() {
        Long themeId = testHelper.insertTheme("공포 테마", "무서운 테마", "https://example.com/theme.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                USERNAME,
                LocalDate.of(2099, 12, 31),
                themeId,
                timeId
        );
        Payment payment = paymentRepository.save(Payment.create(reservationId, 50_000L));
        testHelper.confirmPayment(payment, "payment-key-confirmed");

        List<PaymentHistoryResult> histories = paymentQueryService.findByName(USERNAME);
        PaymentHistoryResult first = histories.getFirst();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(histories).hasSize(1);
            softly.assertThat(first.reservationId()).isEqualTo(reservationId);
            softly.assertThat(first.username()).isEqualTo(USERNAME);
            softly.assertThat(first.date()).isEqualTo(LocalDate.of(2099, 12, 31));
            softly.assertThat(first.theme())
                    .isEqualTo(ThemeResult.from(themeId, "공포 테마", "무서운 테마", "https://example.com/theme.jpg"));
            softly.assertThat(first.time()).isEqualTo(ReservationTimeResult.from(timeId, LocalTime.of(10, 0)));
            softly.assertThat(first.reservationStatus()).isEqualTo("CONFIRMED");
            softly.assertThat(first.orderId()).isEqualTo(payment.getOrderId().value());
            softly.assertThat(first.amount()).isEqualTo(50_000L);
            softly.assertThat(first.paymentKey()).isEqualTo("payment-key-confirmed");
            softly.assertThat(first.paymentStatus()).isEqualTo("CONFIRMED");
        });
    }
}
