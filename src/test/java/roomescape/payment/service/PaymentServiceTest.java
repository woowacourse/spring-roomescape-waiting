package roomescape.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentAmountMismatchException;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentState;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(properties = {
        "spring.sql.init.data-locations=",
        "spring.datasource.url=jdbc:h2:mem:payment-test;DB_CLOSE_DELAY=-1"
})
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @MockBean
    private PaymentGateway paymentGateway;

    private Reservation reservation;
    private String orderId;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.of("user1", "user1@test.com", "1234"));
        ReservationTime time = timeRepository.save(
                ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com", 10000L));
        reservation = reservationRepository.save(
                Reservation.pending(member, LocalDate.now().plusDays(1), time, theme));
        orderId = paymentService.prepare(reservation.getId(), 10000L);
    }

    @Test
    @DisplayName("저장 금액과 다른 amount면 승인 전에 차단되고 게이트웨이는 호출되지 않는다")
    void 금액이_다르면_차단되고_게이트웨이는_호출되지_않는다() {
        assertThatThrownBy(() -> paymentService.confirm("test_pk", orderId, 9000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    @DisplayName("금액이 일치하면 게이트웨이 승인 후 예약 확정·paymentKey 저장이 이뤄진다")
    void 금액이_일치하면_예약이_확정되고_paymentKey가_저장된다() {
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("test_pk_1", orderId, PaymentStatus.DONE, 10000L));

        PaymentResult result = paymentService.confirm("test_pk_1", orderId, 10000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);

        Reservation confirmed = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(confirmed.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        Payment payment = paymentRepository.getByOrderId(orderId);
        assertThat(payment.getPaymentKey()).isEqualTo("test_pk_1");
        assertThat(payment.getState()).isEqualTo(PaymentState.CONFIRMED);
    }
}