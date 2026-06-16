package roomescape.payment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.exception.PaymentAmountMismatchException;
import roomescape.payment.repository.PaymentOrderRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class PaymentServiceTest {
    private static final String NAME = "브라운";
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(1);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Test
    @DisplayName("결제 주문 생성 시 예약 대기열에는 아직 들어가지 않는다.")
    void prepare_success_doesNotCreateReservation() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();

        PaymentReadyOrder paymentReadyOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(paymentReadyOrder.orderId()).orElseThrow();
        assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.READY);
        assertThat(paymentOrder.getAmount()).isEqualTo(1_000L);
        assertThat(countAllReservations()).isZero();
    }

    @Test
    @DisplayName("결제 승인에 성공하면 결제 키를 저장하고 예약을 생성한다.")
    void confirm_success_createsReservationAndStoresPaymentKey() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder paymentReadyOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        when(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .thenAnswer(invocation -> {
                    PaymentConfirmation confirmation = invocation.getArgument(0);
                    return new PaymentResult(
                            confirmation.paymentKey(),
                            confirmation.orderId(),
                            confirmation.amount()
                    );
                });

        Reservation reservation = paymentService.confirm(
                "payment-key-123",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount()
        );

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(paymentReadyOrder.orderId()).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.CONFIRMED);
        assertThat(paymentOrder.getPaymentKey()).isEqualTo("payment-key-123");
        assertThat(paymentOrder.getReservationId()).isEqualTo(reservation.getId());
        assertThat(countAllReservations()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 예약 정보로 결제 대기 주문이 있으면 새 주문을 만들지 않는다.")
    void prepare_fail_whenReadyOrderAlreadyExists() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        assertThatThrownBy(() -> paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 결제 대기 중인 예약 요청이 있습니다.");

        assertThat(countAllPaymentOrders()).isEqualTo(1);
        assertThat(countAllReservations()).isZero();
    }

    @Test
    @DisplayName("콜백 금액이 저장된 주문 금액과 다르면 승인 API를 호출하지 않는다.")
    void confirm_fail_amountMismatchDoesNotCallGateway() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder paymentReadyOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        assertThatThrownBy(() -> paymentService.confirm(
                "payment-key-123",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount() + 1
        ))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
        assertThat(countAllReservations()).isZero();
        assertThat(paymentOrderRepository.findByOrderId(paymentReadyOrder.orderId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.READY);
    }

    @Test
    @DisplayName("실패 콜백에 주문번호가 없으면 아무 작업도 하지 않는다.")
    void fail_success_whenOrderIdIsNull() {
        paymentService.fail(null, "PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.");

        assertThat(countAllPaymentOrders()).isZero();
    }

    @Test
    @DisplayName("실패 콜백은 결제 대기 주문을 실패 상태로 정리한다.")
    void fail_success_marksReadyOrderFailed() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder paymentReadyOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        paymentService.fail(paymentReadyOrder.orderId(), "REJECT_CARD_PAYMENT", "카드 결제가 거절되었습니다.");

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(paymentReadyOrder.orderId()).orElseThrow();
        assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(paymentOrder.getFailureCode()).isEqualTo("REJECT_CARD_PAYMENT");
        assertThat(countAllReservations()).isZero();
    }

    private ReservationTime saveReservationTime(int hour) {
        return reservationTimeRepository.save(ReservationTime.create(LocalTime.of(hour, 0)));
    }

    private Theme saveTheme() {
        return themeRepository.save(Theme.create(
                "결제 탈출",
                "결제 흐름을 검증하는 테마입니다.",
                "https://example.com/payment-theme.png"
        ));
    }

    private Integer countAllReservations() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
    }

    private Integer countAllPaymentOrders() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment_order", Integer.class);
    }
}
