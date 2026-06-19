package roomescape.payment.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.payment.application.dto.OrderInfo;
import roomescape.payment.application.dto.PaymentResult;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.OrderStatus;
import roomescape.payment.infra.client.PaymentStatus;
import roomescape.payment.infra.client.exception.TossBusinessException.PaymentNotFound;
import roomescape.reservation.application.PendingReservationService;
import roomescape.reservation.application.ReservationManager;
import roomescape.reservation.application.ReservationReader;
import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.application.dto.ReservationIntegrationInfo;
import roomescape.reservation.domain.PendingReservation;

@ExtendWith(MockitoExtension.class)
class OrderSchedulerTest {

    @Mock
    private PendingReservationService pendingReservationService;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Clock clock;

    @Mock
    private ReservationManager reservationManager;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderScheduler scheduler;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-06-17T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        given(clock.instant()).willReturn(fixedClock.instant());
        given(clock.getZone()).willReturn(fixedClock.getZone());
        today = LocalDate.now(clock);
    }

    @Test
    @DisplayName("결제가 완료(COMPLETED)된 대기 예약은 토스 환불과 예약 취소가 모두 진행된다.")
    void refundAndCancel_CompletedOrder() {
        // given
        PendingReservation pending = PendingReservation.builder().id(1L).name("포비").build();
        given(pendingReservationService.findExpiredReservations(today)).willReturn(List.of(pending));
        ReservationIntegrationInfo reservationInfo = new ReservationIntegrationInfo(1L, "포비", "테마1", "PENDING");
        OrderInfo completedOrder = OrderInfo.builder()
                .orderId("order-1")
                .amount(50000L)
                .reservation(reservationInfo)
                .status(OrderStatus.COMPLETED)
                .createdAt(today.atStartOfDay())
                .build();
        given(orderService.getOrder(1L)).willReturn(completedOrder);

        // when
        scheduler.processExpiredPendingRefunds();

        // then
        verify(paymentService, times(1)).cancelBySystem(eq("order-1"), anyString());
        verify(pendingReservationService, times(1)).cancel(1L, "포비");
    }

    @Test
    @DisplayName("결제가 진행중이거나 안된(PENDING) 대기 예약은 환불 없이 예약 취소만 진행된다.")
    void onlyCancel_UnpaidOrder() {
        // given
        PendingReservation pending = PendingReservation.builder().id(2L).name("브라운").build();
        given(pendingReservationService.findExpiredReservations(today)).willReturn(List.of(pending));
        ReservationIntegrationInfo reservationInfo = new ReservationIntegrationInfo(2L, "브라운", "테마1", "PENDING");
        OrderInfo unpaidOrder = OrderInfo.builder()
                .orderId("order-2")
                .amount(50000L)
                .reservation(reservationInfo)
                .status(OrderStatus.PENDING)
                .createdAt(today.atStartOfDay())
                .build();
        given(orderService.getOrder(2L)).willReturn(unpaidOrder);

        // when
        scheduler.processExpiredPendingRefunds();

        // then
        verify(paymentService, never()).cancelBySystem(anyString(), anyString());
        verify(pendingReservationService, times(1)).cancel(2L, "브라운");
    }

    @Test
    @DisplayName("중간에 특정 예약 환불 중 에러가 발생해도, 다음 대기 예약의 환불은 정상적으로 진행되어야 한다.")
    void continueProcessing_OnException() {
        // given
        PendingReservation errorPending = PendingReservation.builder().id(1L).name("에러나는사람").build();
        PendingReservation successPending = PendingReservation.builder().id(2L).name("정상인사람").build();
        given(pendingReservationService.findExpiredReservations(today)).willReturn(List.of(errorPending, successPending));

        ReservationIntegrationInfo errorResInfo = new ReservationIntegrationInfo(1L, "에러나는사람", "테마1", "PENDING");
        OrderInfo errorOrder = OrderInfo.builder()
                .orderId("order-1")
                .amount(50000L)
                .reservation(errorResInfo)
                .status(OrderStatus.COMPLETED)
                .createdAt(today.atStartOfDay())
                .build();
        ReservationIntegrationInfo successResInfo = new ReservationIntegrationInfo(2L, "정상인사람", "테마1", "PENDING");
        OrderInfo successOrder = OrderInfo.builder()
                .orderId("order-2")
                .amount(50000L)
                .reservation(successResInfo)
                .status(OrderStatus.COMPLETED)
                .createdAt(today.atStartOfDay())
                .build();
        given(orderService.getOrder(1L)).willReturn(errorOrder);
        given(orderService.getOrder(2L)).willReturn(successOrder);

        doThrow(new RuntimeException("토스 서버 통신 장애")).when(paymentService).cancelBySystem(eq("order-1"), anyString());

        // when
        scheduler.processExpiredPendingRefunds();

        // then
        verify(pendingReservationService, never()).cancel(1L, "에러나는사람");
        verify(paymentService, times(1)).cancelBySystem(eq("order-2"), anyString());
        verify(pendingReservationService, times(1)).cancel(2L, "정상인사람");
    }

    @Test
    @DisplayName("5분이 경과된 미결제 유령 예약은 결제 실패 처리 및 예약 취소가 진행된다.")
    void cleanupUnpaidReservations_Success() {
        // given
        LocalDateTime fiveMinutesAgo = LocalDateTime.now(clock).minusMinutes(5);

        Order abandonedOrder = mock(Order.class);
        given(abandonedOrder.getOrderId()).willReturn("order-1");
        given(abandonedOrder.getReservationId()).willReturn(1L);

        given(orderService.findAbandonedOrders(fiveMinutesAgo)).willReturn(List.of(abandonedOrder));

        ReservationIntegrationInfo resInfo = new ReservationIntegrationInfo(1L, "포비", "테마1", "ACTIVE");
        given(reservationReader.readAll(anyList())).willReturn(Map.of(1L, resInfo));
        given(paymentGateway.getStatus("order-1")).willThrow(new PaymentNotFound("결제 내역 없음"));

        // when
        scheduler.cleanupUnpaidReservations();

        // then
        verify(orderService, times(1)).fail("order-1");
        verify(reservationManager, times(1)).cancelReservation(eq(1L), any(ReservationCancelCommand.class));
    }

    @Test
    @DisplayName("유령 예약 청소 중 특정 예약에서 에러가 발생해도, 다음 예약의 청소는 정상적으로 진행되어야 한다.")
    void cleanupUnpaidReservations_OnException() {
        // given
        LocalDateTime fiveMinutesAgo = LocalDateTime.now(clock).minusMinutes(5);

        Order errorOrder = mock(Order.class);
        given(errorOrder.getOrderId()).willReturn("order-1");
        given(errorOrder.getReservationId()).willReturn(1L);

        Order successOrder = mock(Order.class);
        given(successOrder.getOrderId()).willReturn("order-2");
        given(successOrder.getReservationId()).willReturn(2L);

        given(orderService.findAbandonedOrders(fiveMinutesAgo)).willReturn(List.of(errorOrder, successOrder));

        ReservationIntegrationInfo errorResInfo = new ReservationIntegrationInfo(1L, "에러나는사람", "테마1", "ACTIVE");
        ReservationIntegrationInfo successResInfo = new ReservationIntegrationInfo(2L, "브라운", "테마1", "ACTIVE");

        given(reservationReader.readAll(anyList())).willReturn(Map.of(
                1L, errorResInfo,
                2L, successResInfo
        ));

        given(paymentGateway.getStatus("order-1")).willThrow(new PaymentNotFound("결제 내역 없음"));
        given(paymentGateway.getStatus("order-2")).willThrow(new PaymentNotFound("결제 내역 없음"));

        doThrow(new RuntimeException("결제 DB 업데이트 장애")).when(orderService).fail("order-1");

        // when
        scheduler.cleanupUnpaidReservations();

        // then
        verify(reservationManager, never()).cancelReservation(eq(1L), any());
        verify(orderService, times(1)).fail("order-2");
        verify(reservationManager, times(1)).cancelReservation(eq(2L), any(ReservationCancelCommand.class));
    }

    @Test
    @DisplayName("유령 예약 청소 시 토스에 결제 완료(DONE) 상태로 남아있다면, 예약 취소 전 자동 환불을 먼저 진행한다.")
    void cleanupUnpaidReservations_RefundIfPaid() {
        // given
        LocalDateTime fiveMinutesAgo = LocalDateTime.now(clock).minusMinutes(5);

        Order paidOrder = mock(Order.class);
        given(paidOrder.getOrderId()).willReturn("order-1");
        given(paidOrder.getReservationId()).willReturn(1L);

        given(orderService.findAbandonedOrders(fiveMinutesAgo)).willReturn(List.of(paidOrder));

        ReservationIntegrationInfo resInfo = new ReservationIntegrationInfo(1L, "포비", "테마1", "ACTIVE");
        given(reservationReader.readAll(anyList())).willReturn(Map.of(1L, resInfo));

        PaymentResult tossResult = PaymentResult.builder()
                .paymentKey("test-payment-key")
                .status(PaymentStatus.DONE)
                .approvedAmount(50000L)
                .build();
        given(paymentGateway.getStatus("order-1")).willReturn(tossResult);

        // when
        scheduler.cleanupUnpaidReservations();

        // then
        verify(paymentService, times(1)).cancelBySystem(eq("order-1"), anyString());
        verify(orderService, times(1)).fail("order-1");
        verify(reservationManager, times(1)).cancelReservation(eq(1L), any(ReservationCancelCommand.class));
    }
}
