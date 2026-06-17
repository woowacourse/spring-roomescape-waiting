package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.order.service.OrderService;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.TimeResponse;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeTest {

  @Mock
  ReservationService reservationService;
  @Mock
  ReservationTimeService reservationTimeService;
  @Mock
  OrderService orderService;

  ReservationFacade facade;

  @BeforeEach
  void setUp() {
    facade = new ReservationFacade(reservationService, reservationTimeService, orderService);
  }

  @Nested
  class 예약_생성_시_주문_저장 {

    static final ReservationRequest RESERVATION_REQUEST = new ReservationRequest("누누", "9999-01-01",
        1L, 1L);
    public static final long DEFAULT_RESERVATION_PRICE = 50000L;

    @Test
    void PENDING_상태면_주문이_저장된다() {
      when(reservationService.create(RESERVATION_REQUEST))
          .thenReturn(reservationCreateResponse(1L, ReservationStatus.PENDING));

      facade.createReservation(RESERVATION_REQUEST);

      verify(orderService, times(1)).save(eq(1L), any(), eq(DEFAULT_RESERVATION_PRICE));
    }

    @Test
    void WAITING_상태면_주문이_저장되지_않는다() {
      when(reservationService.create(RESERVATION_REQUEST))
          .thenReturn(reservationCreateResponse(2L, ReservationStatus.WAITING));

      facade.createReservation(RESERVATION_REQUEST);

      verify(orderService, never()).save(any(), any(), any());
    }

    @Test
    void 생성되는_orderId는_6자_이상_64자_이하의_영숫자와_하이픈만_허용한다() {
      when(reservationService.create(RESERVATION_REQUEST))
          .thenReturn(reservationCreateResponse(1L, ReservationStatus.PENDING));

      ArgumentCaptor<String> orderIdCaptor = ArgumentCaptor.forClass(String.class);
      facade.createReservation(RESERVATION_REQUEST);
      verify(orderService).save(any(), orderIdCaptor.capture(), any());

      String orderId = orderIdCaptor.getValue();
      assertThat(orderId).matches("[a-zA-Z0-9\\-_]{6,64}");
    }
  }

  private ReservationCreateResponse reservationCreateResponse(Long id, ReservationStatus status) {
    return new ReservationCreateResponse(id, "누누", LocalDate.of(9999, 1, 1),
        new TimeResponse(1L, LocalTime.of(10, 0)), status);
  }
}