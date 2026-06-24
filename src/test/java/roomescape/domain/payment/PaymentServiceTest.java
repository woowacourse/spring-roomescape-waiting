package roomescape.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.RoomescapeException;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void 결제_승인_요청을_클라이언트에_위임한다() {
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 1000L);
        PaymentConfirmResponse expected = new PaymentConfirmResponse("paymentKey", "orderId", 1000L, "DONE");
        ReservationPayment payment = new ReservationPayment(
            1L,
            1L,
            "orderId",
            null,
            1000L,
            "fixed-idempotency-key",
            PaymentStatus.PENDING
        );
        given(paymentRepository.findByOrderId("orderId")).willReturn(Optional.of(payment));
        given(paymentClient.confirm(request, "fixed-idempotency-key")).willReturn(expected);

        PaymentConfirmResponse response = paymentService.confirm(request);

        assertThat(response).isEqualTo(expected);
        verify(paymentClient).confirm(request, "fixed-idempotency-key");
        verify(paymentRepository).updateConfirmed("orderId", "paymentKey", 1000L);
    }

    @Test
    void read_timeout이면_결제_상태를_확인_필요로_변경한다() {
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 1000L);
        ReservationPayment payment = new ReservationPayment(
            1L,
            1L,
            "orderId",
            null,
            1000L,
            "fixed-idempotency-key",
            PaymentStatus.PENDING
        );
        given(paymentRepository.findByOrderId("orderId")).willReturn(Optional.of(payment));
        given(paymentClient.confirm(request, "fixed-idempotency-key"))
            .willThrow(new PaymentResultUnknownException());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> paymentService.confirm(request))
            .isInstanceOf(PaymentResultUnknownException.class);

        verify(paymentRepository).updateRequiresConfirmation("orderId", "paymentKey");
    }

    @Test
    void 이미_확정된_주문은_토스를_다시_호출하지_않는다() {
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 1000L);
        ReservationPayment payment = new ReservationPayment(
            1L,
            1L,
            "orderId",
            "paymentKey",
            1000L,
            "fixed-idempotency-key",
            PaymentStatus.CONFIRMED
        );
        given(paymentRepository.findByOrderId("orderId")).willReturn(Optional.of(payment));

        PaymentConfirmResponse response = paymentService.confirm(request);

        assertThat(response.status()).isEqualTo("DONE");
        verify(paymentClient, never()).confirm(request, "fixed-idempotency-key");
    }

    @Test
    void 저장된_주문_금액과_다르면_승인하지_않는다() {
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 2000L);
        ReservationPayment payment = new ReservationPayment(
            1L,
            1L,
            "orderId",
            null,
            1000L,
            "fixed-idempotency-key",
            PaymentStatus.PENDING
        );
        given(paymentRepository.findByOrderId("orderId")).willReturn(Optional.of(payment));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> paymentService.confirm(request))
            .isInstanceOf(RoomescapeException.class);

        verify(paymentClient, never()).confirm(request, "fixed-idempotency-key");
    }
}
