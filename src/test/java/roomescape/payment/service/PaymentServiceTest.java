package roomescape.payment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidRequestException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderDetails;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentAmountMismatchException;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.repository.PaymentOrderRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class PaymentServiceTest {
    private static final String NAME = "브라운";
    private static final String OTHER_NAME = "샐리";
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

    @Autowired
    private ReservationService reservationService;

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
    @DisplayName("이미 확정된 주문을 같은 결제 키로 다시 승인하면 기존 예약을 반환한다.")
    void confirm_success_whenOrderAlreadyConfirmedWithSamePaymentKey() {
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

        Reservation firstReservation = paymentService.confirm(
                "payment-key-123",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount()
        );
        Reservation secondReservation = paymentService.confirm(
                "payment-key-123",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount()
        );

        assertThat(secondReservation.getId()).isEqualTo(firstReservation.getId());
        assertThat(countAllReservations()).isEqualTo(1);
        verify(paymentGateway, times(1)).confirm(any(PaymentConfirmation.class));
    }

    @Test
    @DisplayName("이미 확정된 주문을 다른 결제 키로 다시 승인하면 실패한다.")
    void confirm_fail_whenOrderAlreadyConfirmedWithDifferentPaymentKey() {
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

        paymentService.confirm(
                "payment-key-123",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount()
        );

        assertThatThrownBy(() -> paymentService.confirm(
                "other-payment-key",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount()
        ))
                .isInstanceOf(PaymentAlreadyProcessedException.class)
                .hasMessage("이미 다른 결제 키로 승인된 결제입니다.");

        assertThat(countAllReservations()).isEqualTo(1);
        verify(paymentGateway, times(1)).confirm(any(PaymentConfirmation.class));
    }

    @Test
    @DisplayName("승인 API가 이미 처리됨을 응답해도 로컬 주문이 같은 결제 키로 확정되어 있으면 기존 예약을 반환한다.")
    void confirm_success_whenGatewayAlreadyProcessedAndLocalOrderIsConfirmed() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder paymentReadyOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        when(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .thenAnswer(invocation -> {
                    PaymentConfirmation confirmation = invocation.getArgument(0);
                    Reservation reservation = reservationService.create(NAME, FUTURE_DATE, time.getId(), theme.getId());
                    PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(confirmation.orderId()).orElseThrow();
                    paymentOrderRepository.update(paymentOrder.confirm(
                            confirmation.paymentKey(),
                            reservation.getId(),
                            LocalDateTime.now()
                    ));

                    throw new PaymentAlreadyProcessedException("이미 승인된 결제입니다.");
                });

        Reservation reservation = paymentService.confirm(
                "payment-key-123",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount()
        );

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(paymentReadyOrder.orderId()).orElseThrow();
        assertThat(reservation.getId()).isEqualTo(paymentOrder.getReservationId());
        assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.CONFIRMED);
        assertThat(countAllReservations()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 예약 정보로 결제 대기 주문이 있어도 새 결제 시도를 만들 수 있다.")
    void prepare_success_whenReadyOrderAlreadyExists() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder firstOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        PaymentReadyOrder secondOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        assertThat(secondOrder.orderId()).isNotEqualTo(firstOrder.orderId());
        assertThat(countAllPaymentOrders()).isEqualTo(2);
        assertThat(countAllReservations()).isZero();
    }

    @Test
    @DisplayName("이미 확정된 같은 예약이 있으면 결제 승인 API를 호출하지 않는다.")
    void confirm_fail_whenSameReservationAlreadyConfirmedDoesNotCallGatewayAgain() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder firstOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());
        PaymentReadyOrder secondOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        when(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .thenAnswer(invocation -> {
                    PaymentConfirmation confirmation = invocation.getArgument(0);
                    return new PaymentResult(
                            confirmation.paymentKey(),
                            confirmation.orderId(),
                            confirmation.amount()
                    );
                });
        paymentService.confirm("payment-key-123", firstOrder.orderId(), firstOrder.amount());
        clearInvocations(paymentGateway);

        assertThatThrownBy(() -> paymentService.confirm("payment-key-456", secondOrder.orderId(), secondOrder.amount()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 같은 날짜, 시간, 테마에 예약 또는 대기가 있습니다.");

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
        assertThat(countAllPaymentOrders()).isEqualTo(2);
        assertThat(countAllReservations()).isEqualTo(1);
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
    @DisplayName("결제 승인 응답의 주문번호가 요청 주문번호와 다르면 예약을 생성하지 않는다.")
    void confirm_fail_whenGatewayOrderIdMismatchDoesNotCreateReservation() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder paymentReadyOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        when(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .thenReturn(new PaymentResult("payment-key-123", "other-order-id", paymentReadyOrder.amount()));

        assertThatThrownBy(() -> paymentService.confirm(
                "payment-key-123",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount()
        ))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessage("결제 승인 응답 주문번호가 요청 주문번호와 일치하지 않습니다.");

        assertThat(countAllReservations()).isZero();
        assertThat(paymentOrderRepository.findByOrderId(paymentReadyOrder.orderId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.READY);
    }

    @Test
    @DisplayName("결제 승인 응답의 결제 키가 요청 키와 다르면 예약을 생성하지 않는다.")
    void confirm_fail_whenGatewayPaymentKeyMismatchDoesNotCreateReservation() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder paymentReadyOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());

        when(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .thenReturn(new PaymentResult("other-payment-key", paymentReadyOrder.orderId(), paymentReadyOrder.amount()));

        assertThatThrownBy(() -> paymentService.confirm(
                "payment-key-123",
                paymentReadyOrder.orderId(),
                paymentReadyOrder.amount()
        ))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessage("결제 승인 응답 키가 요청 키와 일치하지 않습니다.");

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

    @Test
    @DisplayName("이름으로 결제 주문 내역을 조회한다.")
    void findOrdersByName_success() {
        ReservationTime time = saveReservationTime(10);
        Theme theme = saveTheme();
        PaymentReadyOrder failedOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());
        PaymentReadyOrder confirmedOrder = paymentService.prepare(NAME, FUTURE_DATE, time.getId(), theme.getId());
        paymentService.prepare(OTHER_NAME, FUTURE_DATE, time.getId(), theme.getId());

        paymentService.fail(failedOrder.orderId(), "REJECT_CARD_PAYMENT", "카드 결제가 거절되었습니다.");
        when(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .thenAnswer(invocation -> {
                    PaymentConfirmation confirmation = invocation.getArgument(0);
                    return new PaymentResult(
                            confirmation.paymentKey(),
                            confirmation.orderId(),
                            confirmation.amount()
                    );
                });
        paymentService.confirm("payment-key-123", confirmedOrder.orderId(), confirmedOrder.amount());

        List<PaymentOrderDetails> orders = paymentService.findOrdersByName(NAME);

        assertThat(orders).hasSize(2);
        assertThat(orders)
                .extracting(PaymentOrderDetails::orderId)
                .containsExactly(confirmedOrder.orderId(), failedOrder.orderId());
        assertThat(orders)
                .extracting(PaymentOrderDetails::status)
                .containsExactly(PaymentOrderStatus.CONFIRMED, PaymentOrderStatus.FAILED);
        assertThat(orders.get(0).theme().getName()).isEqualTo("결제 탈출");
        assertThat(orders.get(0).time().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(orders.get(1).failureMessage()).isEqualTo("카드 결제가 거절되었습니다.");
    }

    @Test
    @DisplayName("결제 주문 내역 조회 이름은 비어 있을 수 없다.")
    void findOrdersByName_fail_whenNameIsBlank() {
        assertThatThrownBy(() -> paymentService.findOrdersByName(" "))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("조회할 이름을 입력해 주세요.");
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
