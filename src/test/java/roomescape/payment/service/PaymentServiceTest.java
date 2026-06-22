package roomescape.payment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.common.exception.UnprocessableContentException;
import roomescape.payment.controller.dto.request.PaymentConfirmRequest;
import roomescape.payment.controller.dto.request.PaymentFailRequest;
import roomescape.payment.controller.dto.response.PaymentConfirmResponse;
import roomescape.payment.controller.dto.response.PaymentReadyResponse;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.repository.PaymentOrderRepository;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Sql("/clear.sql")
class PaymentServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    PaymentOrderRepository paymentOrderRepository;

    @MockitoBean
    PaymentGateway paymentGateway;

    @Test
    @DisplayName("예약 생성 요청 시 결제 대기 예약과 주문 정보를 저장한다")
    void preparePendingReservationAndPaymentOrder() {
        // given
        final PaymentReadyResponse paymentReadyResponse = preparePayment(22000);

        // when
        final Reservation reservation = reservationRepository.findById(paymentReadyResponse.reservationId())
                .orElseThrow();
        final PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(paymentReadyResponse.orderId())
                .orElseThrow();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.READY);
        assertThat(paymentOrder.getAmount()).isEqualTo(22000);
        assertThat(paymentOrder.getIdempotencyKey()).isNotBlank();
    }

    @Test
    @DisplayName("콜백 금액이 저장 금액과 다르면 승인 API를 호출하지 않는다")
    void doNotCallGatewayWhenAmountMismatch() {
        // given
        final PaymentReadyResponse paymentReadyResponse = preparePayment(22000);

        // when & then
        assertThatThrownBy(() -> paymentService.confirm(
                new PaymentConfirmRequest("payment-key", paymentReadyResponse.orderId(), 22001)
        ))
                .isInstanceOf(UnprocessableContentException.class);
        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
    }

    @Test
    @DisplayName("결제 승인에 성공하면 paymentKey를 저장하고 예약을 확정한다")
    void confirmPayment() {
        // given
        final PaymentReadyResponse paymentReadyResponse = preparePayment(22000);
        final PaymentOrder readyPaymentOrder = paymentOrderRepository.findByOrderId(paymentReadyResponse.orderId())
                .orElseThrow();
        when(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .thenReturn(new PaymentResult("payment-key", paymentReadyResponse.orderId(), 22000));

        // when
        final PaymentConfirmResponse response = paymentService.confirm(
                new PaymentConfirmRequest("payment-key", paymentReadyResponse.orderId(), 22000)
        );

        // then
        final Reservation reservation = reservationRepository.findById(paymentReadyResponse.reservationId())
                .orElseThrow();
        final PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(paymentReadyResponse.orderId())
                .orElseThrow();

        assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED.name());
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(paymentOrder.getStatus()).isEqualTo(PaymentOrderStatus.COMPLETED);
        assertThat(paymentOrder.getPaymentKey()).isEqualTo("payment-key");

        final ArgumentCaptor<PaymentConfirmation> captor = ArgumentCaptor.forClass(PaymentConfirmation.class);
        verify(paymentGateway).confirm(captor.capture());
        assertThat(captor.getValue().idempotencyKey()).isEqualTo(readyPaymentOrder.getIdempotencyKey());
    }

    @Test
    @DisplayName("failUrl에 orderId가 없어도 예외가 발생하지 않는다")
    void handleFailWithoutOrderId() {
        assertThatCode(() -> paymentService.fail(new PaymentFailRequest(
                "PAY_PROCESS_CANCELED",
                "사용자가 결제를 취소했습니다.",
                null
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("결제 실패 시 결제 대기 주문과 예약을 정리한다")
    void deletePendingReservationAndOrderWhenPaymentFails() {
        // given
        final PaymentReadyResponse paymentReadyResponse = preparePayment(22000);

        // when
        paymentService.fail(new PaymentFailRequest(
                "REJECT_CARD_PAYMENT",
                "카드 결제가 거절되었습니다.",
                paymentReadyResponse.orderId()
        ));

        // then
        assertThat(paymentOrderRepository.findByOrderId(paymentReadyResponse.orderId())).isEmpty();
        assertThat(reservationRepository.findById(paymentReadyResponse.reservationId())).isEmpty();
    }

    private PaymentReadyResponse preparePayment(final int price) {
        final Long timeId = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(11, 0)))
                .getId();
        final Long themeId = themeRepository.save(Theme.create("링", "공포 테마", "http:~", price))
                .getId();

        return reservationService.preparePayment(
                new ReservationCreateRequest(
                        "브라운",
                        "customer@example.com",
                        LocalDate.now().plusDays(1),
                        timeId,
                        themeId
                )
        );
    }
}
