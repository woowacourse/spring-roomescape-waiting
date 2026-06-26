package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationStatus;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.PaymentOrderRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationTimeResult;
import roomescape.service.dto.ThemeResult;

@ExtendWith(MockitoExtension.class)
class ReservationConfirmerTest {

    private static final LocalDate FUTURE_DATE = LocalDate.of(2099, 12, 31);

    @Mock
    private AdminReservationService reservationService;
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    @InjectMocks
    private ReservationConfirmer reservationConfirmer;

    @Test
    @DisplayName("주문 정보로 예약을 생성하고 주문을 확정 처리한다")
    void confirmReservation() {
        PaymentOrder order = PaymentOrder.pending("order-1", "카프카", FUTURE_DATE, 1L, 1L, 1000L);
        ReservationResult created = new ReservationResult(
                10L, "카프카", FUTURE_DATE,
                new ReservationTimeResult(1L, LocalTime.of(10, 0)),
                new ThemeResult(1L, "무인도 탈출", "설명", "https://example.com/thumb.jpg"),
                0L, ReservationStatus.CONFIRMED);
        given(paymentOrderRepository.getByOrderId("order-1")).willReturn(order);
        given(reservationService.create(any(ReservationCreateCommand.class))).willReturn(created);

        ReservationResult result = reservationConfirmer.confirmReservation("order-1", "test_pk_1");

        assertThat(result.id()).isEqualTo(10L);
        verify(reservationService, times(1)).create(any(ReservationCreateCommand.class));
        verify(paymentOrderRepository, times(1)).markConfirmed("order-1", "test_pk_1", 10L);
    }
}
