package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.PaymentAlreadyProcessedException;
import roomescape.global.exception.PaymentAmountMismatchException;
import roomescape.global.exception.PaymentNotFoundException;
import roomescape.global.exception.RetryablePaymentGatewayException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.reservation.application.dto.PaymentConfirmCommand;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import roomescape.reservation.application.port.out.payment.PaymentGateway;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.application.port.out.payment.PaymentStatus;
import roomescape.reservation.application.service.PaymentCommandService;
import roomescape.reservation.domain.Payment;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;

@ServiceTest
class PaymentCommandServiceTest {

    private static final String PAYMENT_KEY = "test_payment_key";

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private TestDataHelper testHelper;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @DisplayName("결제 승인에 성공하면 결제 주문과 예약 상태를 확정으로 변경한다.")
    @Test
    void confirm_payment() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult(PAYMENT_KEY, payment.getOrderId().value(), PaymentStatus.DONE,
                        payment.getAmount().value()));

        PaymentResult result = paymentCommandService.confirm(paymentConfirmCommand(payment));

        ArgumentCaptor<PaymentConfirmation> confirmationCaptor = ArgumentCaptor.forClass(PaymentConfirmation.class);
        verify(paymentGateway).confirm(confirmationCaptor.capture());
        PaymentConfirmation confirmation = confirmationCaptor.getValue();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentKey(payment.getOrderId().value())).isEqualTo(PAYMENT_KEY);
            softly.assertThat(confirmation.paymentKey()).isEqualTo(PAYMENT_KEY);
            softly.assertThat(confirmation.orderId()).isEqualTo(payment.getOrderId().value());
            softly.assertThat(confirmation.amount()).isEqualTo(payment.getAmount().value());
        });
    }

    @DisplayName("결제 승인 결과가 DONE이 아니면 결제 주문과 예약 상태를 확정하지 않는다.")
    @Test
    void confirm_payment_non_done_result() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult(PAYMENT_KEY, payment.getOrderId().value(), PaymentStatus.ABORTED,
                        payment.getAmount().value()));

        assertThatThrownBy(() -> paymentCommandService.confirm(paymentConfirmCommand(payment)))
                .isInstanceOf(RoomEscapeException.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId()))
                    .isEqualTo("PAYMENT_PENDING");
        });
    }

    @DisplayName("결제 승인 게이트웨이에서 예외가 발생하면 결제 주문과 예약 상태를 확정하지 않는다.")
    @Test
    void confirm_payment_gateway_exception() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willThrow(new RuntimeException("gateway error"));

        assertThatThrownBy(() -> paymentCommandService.confirm(paymentConfirmCommand(payment)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("gateway error");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId()))
                    .isEqualTo("PAYMENT_PENDING");
        });
    }

    @DisplayName("결제 승인 재시도 실패 후 orderId 조회 결과가 완료 상태이면 결제 주문과 예약 상태를 확정한다.")
    @Test
    void confirm_payment_reconciles_retryable_failure_by_order_id() {
        Payment payment = preparePayment();
        String resolvedPaymentKey = "resolved_payment_key";
        given(paymentGateway.confirm(any()))
                .willThrow(new RetryablePaymentGatewayException());
        given(paymentGateway.findByOrderId(payment.getOrderId().value()))
                .willReturn(new PaymentResult(resolvedPaymentKey, payment.getOrderId().value(), PaymentStatus.DONE,
                        payment.getAmount().value()));

        PaymentResult result = paymentCommandService.confirm(paymentConfirmCommand(payment));

        verify(paymentGateway).findByOrderId(payment.getOrderId().value());
        verify(paymentGateway, never()).findByPaymentKey(anyString());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.paymentKey()).isEqualTo(resolvedPaymentKey);
            softly.assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentKey(payment.getOrderId().value())).isEqualTo(resolvedPaymentKey);
        });
    }

    @DisplayName("orderId 조회로 결제를 찾지 못하면 paymentKey 조회 결과로 결제 주문과 예약 상태를 보정한다.")
    @Test
    void confirm_payment_reconciles_retryable_failure_by_payment_key_fallback() {
        Payment payment = preparePayment();
        String resolvedPaymentKey = "resolved_payment_key";
        given(paymentGateway.confirm(any()))
                .willThrow(new RetryablePaymentGatewayException());
        given(paymentGateway.findByOrderId(payment.getOrderId().value()))
                .willThrow(new PaymentNotFoundException());
        given(paymentGateway.findByPaymentKey(PAYMENT_KEY))
                .willReturn(new PaymentResult(resolvedPaymentKey, payment.getOrderId().value(), PaymentStatus.DONE,
                        payment.getAmount().value()));

        PaymentResult result = paymentCommandService.confirm(paymentConfirmCommand(payment));

        verify(paymentGateway).findByOrderId(payment.getOrderId().value());
        verify(paymentGateway).findByPaymentKey(PAYMENT_KEY);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.paymentKey()).isEqualTo(resolvedPaymentKey);
            softly.assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentKey(payment.getOrderId().value())).isEqualTo(resolvedPaymentKey);
        });
    }

    @DisplayName("이미 처리된 결제 응답 후 orderId 조회 결과가 완료 상태이면 결제 주문과 예약 상태를 확정한다.")
    @Test
    void confirm_payment_reconciles_already_processed_by_order_id() {
        Payment payment = preparePayment();
        String resolvedPaymentKey = "resolved_payment_key";
        given(paymentGateway.confirm(any()))
                .willThrow(new PaymentAlreadyProcessedException());
        given(paymentGateway.findByOrderId(payment.getOrderId().value()))
                .willReturn(new PaymentResult(resolvedPaymentKey, payment.getOrderId().value(), PaymentStatus.DONE,
                        payment.getAmount().value()));

        PaymentResult result = paymentCommandService.confirm(paymentConfirmCommand(payment));

        verify(paymentGateway).findByOrderId(payment.getOrderId().value());
        verify(paymentGateway, never()).findByPaymentKey(anyString());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.paymentKey()).isEqualTo(resolvedPaymentKey);
            softly.assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentKey(payment.getOrderId().value())).isEqualTo(resolvedPaymentKey);
        });
    }

    @DisplayName("이미 처리된 결제 응답 후 orderId 조회로 찾지 못하면 paymentKey 조회 결과로 결제 주문과 예약 상태를 보정한다.")
    @Test
    void confirm_payment_reconciles_already_processed_by_payment_key_fallback() {
        Payment payment = preparePayment();
        String resolvedPaymentKey = "resolved_payment_key";
        given(paymentGateway.confirm(any()))
                .willThrow(new PaymentAlreadyProcessedException());
        given(paymentGateway.findByOrderId(payment.getOrderId().value()))
                .willThrow(new PaymentNotFoundException());
        given(paymentGateway.findByPaymentKey(PAYMENT_KEY))
                .willReturn(new PaymentResult(resolvedPaymentKey, payment.getOrderId().value(), PaymentStatus.DONE,
                        payment.getAmount().value()));

        PaymentResult result = paymentCommandService.confirm(paymentConfirmCommand(payment));

        verify(paymentGateway).findByOrderId(payment.getOrderId().value());
        verify(paymentGateway).findByPaymentKey(PAYMENT_KEY);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.paymentKey()).isEqualTo(resolvedPaymentKey);
            softly.assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentKey(payment.getOrderId().value())).isEqualTo(resolvedPaymentKey);
        });
    }

    @DisplayName("결제 승인 재시도 실패 후 조회 결과가 완료 상태가 아니면 기존 재시도 가능 예외를 유지한다.")
    @Test
    void confirm_payment_keeps_retryable_failure_when_inquiry_is_not_done() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willThrow(new RetryablePaymentGatewayException());
        given(paymentGateway.findByOrderId(payment.getOrderId().value()))
                .willReturn(new PaymentResult(PAYMENT_KEY, payment.getOrderId().value(), PaymentStatus.ABORTED,
                        payment.getAmount().value()));

        assertThatThrownBy(() -> paymentCommandService.confirm(paymentConfirmCommand(payment)))
                .isInstanceOf(RetryablePaymentGatewayException.class)
                .hasMessage("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.");

        verify(paymentGateway).findByOrderId(payment.getOrderId().value());
        verify(paymentGateway, never()).findByPaymentKey(anyString());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId()))
                    .isEqualTo("PAYMENT_PENDING");
        });
    }

    @DisplayName("이미 처리된 결제 응답 후 조회 결과가 완료 상태가 아니면 기존 이미 처리된 결제 예외를 유지한다.")
    @Test
    void confirm_payment_keeps_already_processed_when_inquiry_is_not_done() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willThrow(new PaymentAlreadyProcessedException());
        given(paymentGateway.findByOrderId(payment.getOrderId().value()))
                .willReturn(new PaymentResult(PAYMENT_KEY, payment.getOrderId().value(), PaymentStatus.ABORTED,
                        payment.getAmount().value()));

        assertThatThrownBy(() -> paymentCommandService.confirm(paymentConfirmCommand(payment)))
                .isInstanceOf(PaymentAlreadyProcessedException.class)
                .hasMessage("이미 승인된 결제입니다.");

        verify(paymentGateway).findByOrderId(payment.getOrderId().value());
        verify(paymentGateway, never()).findByPaymentKey(anyString());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId()))
                    .isEqualTo("PAYMENT_PENDING");
        });
    }

    @DisplayName("결제 승인 재시도 실패 후 조회 결과의 주문번호가 다르면 결제 주문과 예약 상태를 확정하지 않는다.")
    @Test
    void confirm_payment_keeps_retryable_failure_when_inquiry_order_id_mismatches() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willThrow(new RetryablePaymentGatewayException());
        given(paymentGateway.findByOrderId(payment.getOrderId().value()))
                .willReturn(new PaymentResult(PAYMENT_KEY, "other-order-id", PaymentStatus.DONE,
                        payment.getAmount().value()));

        assertThatThrownBy(() -> paymentCommandService.confirm(paymentConfirmCommand(payment)))
                .isInstanceOf(RetryablePaymentGatewayException.class)
                .hasMessage("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.");

        verify(paymentGateway).findByOrderId(payment.getOrderId().value());
        verify(paymentGateway, never()).findByPaymentKey(anyString());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId()))
                    .isEqualTo("PAYMENT_PENDING");
        });
    }

    @DisplayName("결제 승인 재시도 실패 후 조회 결과의 금액이 다르면 결제 주문과 예약 상태를 확정하지 않는다.")
    @Test
    void confirm_payment_keeps_retryable_failure_when_inquiry_amount_mismatches() {
        Payment payment = preparePayment();
        given(paymentGateway.confirm(any()))
                .willThrow(new RetryablePaymentGatewayException());
        given(paymentGateway.findByOrderId(payment.getOrderId().value()))
                .willReturn(new PaymentResult(PAYMENT_KEY, payment.getOrderId().value(), PaymentStatus.DONE,
                        payment.getAmount().value() + 1));

        assertThatThrownBy(() -> paymentCommandService.confirm(paymentConfirmCommand(payment)))
                .isInstanceOf(RetryablePaymentGatewayException.class)
                .hasMessage("결제 서비스가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.");

        verify(paymentGateway).findByOrderId(payment.getOrderId().value());
        verify(paymentGateway, never()).findByPaymentKey(anyString());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId()))
                    .isEqualTo("PAYMENT_PENDING");
        });
    }

    @DisplayName("저장된 결제 금액과 요청 금액이 다르면 승인 호출 전에 차단한다.")
    @Test
    void confirm_payment_amount_mismatch() {
        Payment payment = preparePayment();

        assertThatThrownBy(() -> paymentCommandService.confirm(paymentConfirmCommand(payment, 1_000L)))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("PENDING");
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId()))
                    .isEqualTo("PAYMENT_PENDING");
        });
    }

    @DisplayName("이미 확정된 결제 주문은 승인 호출 전에 차단하고 기존 결제 키를 유지한다.")
    @Test
    void confirm_payment_already_confirmed_order() {
        Payment payment = preparePayment();
        String savedPaymentKey = "already_confirmed_payment_key";
        testHelper.confirmPayment(payment, savedPaymentKey);

        assertThatThrownBy(() -> paymentCommandService.confirm(paymentConfirmCommand(payment)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 처리된 결제입니다.");

        verify(paymentGateway, never()).confirm(any());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(testHelper.findPaymentStatus(payment.getOrderId().value())).isEqualTo("CONFIRMED");
            softly.assertThat(testHelper.findPaymentKey(payment.getOrderId().value())).isEqualTo(savedPaymentKey);
            softly.assertThat(testHelper.findReservationStatus(payment.getReservationId())).isEqualTo("CONFIRMED");
        });
    }

    private Payment preparePayment() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        Long reservationId = testHelper.insertReservation(
                "스타크",
                ReservationFixture.futureReservationDate(),
                themeId,
                timeId
        );

        return paymentCommandService.prepare(reservationId);
    }

    private PaymentConfirmCommand paymentConfirmCommand(Payment payment) {
        return paymentConfirmCommand(payment, payment.getAmount().value());
    }

    private PaymentConfirmCommand paymentConfirmCommand(Payment payment, Long amount) {
        return new PaymentConfirmCommand(PAYMENT_KEY, payment.getOrderId().value(), amount);
    }
}
