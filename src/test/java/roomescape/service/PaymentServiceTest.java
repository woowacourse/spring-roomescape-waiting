package roomescape.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.OrderId;
import roomescape.domain.Payment;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.command.PaymentCreateCommand;
import roomescape.service.dto.result.PaymentReadyResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

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
        Payment savedPayment = Payment.from(1L, orderId, reservationId, price, null);
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
}
