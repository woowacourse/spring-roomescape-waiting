package roomescape.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import roomescape.domain.OrderId;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.ReservationConfirmationEvent;
import roomescape.service.dto.command.PaymentCreateCommand;
import roomescape.service.dto.command.PaymentSuccessCommand;
import roomescape.service.dto.result.PaymentConfirmResult;
import roomescape.service.dto.result.PaymentReadyResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void 주문정보_생성() {
        // given
        Long reservationId = 1L;
        Long price = 30000L;
        PaymentCreateCommand command = new PaymentCreateCommand(
                reservationId, price
        );

        given(reservationRepository.existsById(reservationId)).willReturn(true);

        OrderId orderId = OrderId.generate();
        Payment savedPayment = Payment.from(1L, orderId, reservationId, price, null, PaymentStatus.READY);
        given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);

        // when
        PaymentReadyResult result = paymentService.create(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.reservationId()).isEqualTo(reservationId);
        assertThat(result.orderId()).startsWith("order-");
        assertThat(result.amount()).isEqualTo(price);
    }

    @Test
    void 존재하지_않는_예약에_대해_주문정보_생성을_시도할_경우_예외발생() {
        // given
        Long targetReservationId = 1L;
        Long price = 30000L;
        PaymentCreateCommand command = new PaymentCreateCommand(
                targetReservationId, price
        );
        given(reservationRepository.existsById(targetReservationId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> paymentService.create(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_FOUND);

        verify(paymentRepository, org.mockito.Mockito.never()).save(any(Payment.class));
    }

    @Test
    void 금액이_검증되면_결제가_승인되고_예약_확정_이벤트가_발생한다() {
        // given
        Long reservationId = 1L;

        String orderIdString = "order-1";
        Long price = 30000L;
        String paymentKey = "test-paymentKey-result";

        PaymentSuccessCommand command = new PaymentSuccessCommand(
                orderIdString, price, paymentKey
        );

        OrderId orderId = new OrderId(orderIdString);

        Payment retrievedPayment = Payment.from(1L, orderId, reservationId, price, null, PaymentStatus.READY);
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(retrievedPayment));

        // when
        PaymentConfirmResult result = paymentService.confirm(command);

        // then
        assertThat(result.orderId()).isEqualTo(orderId.id());
        assertThat(result.approvedAmount()).isEqualTo(price);
        assertThat(result.paymentKey()).isEqualTo(paymentKey);
        verify(paymentRepository).update(any(Payment.class));
        verify(eventPublisher).publishEvent(any(ReservationConfirmationEvent.class));
    }

    @Test
    void 해당_주문번호의_결제정보가_존재하지_않으면_예외가_발생하고_예약_확정_이벤트가_미발생한다() {
        // given
        String orderIdString = "order-1";
        OrderId orderId = new OrderId(orderIdString);
        Long price = 30000L;
        String paymentKey = "test-paymentKey-result";

        PaymentSuccessCommand command = new PaymentSuccessCommand(
                orderIdString, price, paymentKey
        );

        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.empty());

        // when & them
        assertThatThrownBy(() -> paymentService.confirm(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_NOT_FOUND);
        verify(paymentRepository, never()).update(any(Payment.class));
        verify(eventPublisher, never()).publishEvent(any(ReservationConfirmationEvent.class));
    }

    @Test
    void 금액_검증에_실패하면_예외가_발생하고_예약_확정_이벤트가_미발생한다() {
        // given
        Long reservationId = 1L;

        String orderIdString = "order-1";
        OrderId orderId = new OrderId(orderIdString);
        Long wrongPrice = 100000L;
        Long price = 30000L;
        String paymentKey = "test-paymentKey-result";

        PaymentSuccessCommand command = new PaymentSuccessCommand(
                orderIdString, wrongPrice, paymentKey
        );

        Payment retrievedPayment = Payment.from(1L, orderId, reservationId, price, null, PaymentStatus.READY);
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(retrievedPayment));

        // when & them
        assertThatThrownBy(() -> paymentService.confirm(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        verify(eventPublisher, never()).publishEvent(any(ReservationConfirmationEvent.class));
    }
}
