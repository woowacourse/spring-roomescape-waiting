package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import roomescape.controller.dto.payment.PaymentConfirmResponse;
import roomescape.controller.dto.payment.PaymentOrderRequest;
import roomescape.controller.dto.payment.PaymentOrderResponse;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.payment.PaymentCheckoutProperties;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentOrder;
import roomescape.payment.PaymentOrderIdGenerator;
import roomescape.payment.PaymentOrderStatus;
import roomescape.repository.MemberDao;
import roomescape.repository.PaymentOrderDao;
import roomescape.repository.ReservationDao;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentOrderDao paymentOrderDao;

    @Mock
    private MemberDao memberDao;

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentOrderIdGenerator orderIdGenerator;

    private final PaymentCheckoutProperties checkoutProperties = new PaymentCheckoutProperties("test_ck_123");

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentOrderDao,
                memberDao,
                reservationDao,
                scheduleService,
                reservationService,
                paymentGateway,
                orderIdGenerator,
                checkoutProperties
        );
    }

    @DisplayName("결제 주문 금액은 테마 가격으로 저장한다.")
    @Test
    void createOrderUsesThemePrice() {
        Member member = member(1L);
        Schedule schedule = schedule(10L, 23000);
        PaymentOrderRequest request = new PaymentOrderRequest(schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(0);
        given(orderIdGenerator.generate()).willReturn("order-123456");
        ArgumentCaptor<PaymentOrder> orderCaptor = ArgumentCaptor.forClass(PaymentOrder.class);

        PaymentOrderResponse response = paymentService.createOrder(request, member, "http://localhost:8080");

        assertThat(response.clientKey()).isEqualTo("test_ck_123");
        assertThat(response.orderId()).isEqualTo("order-123456");
        assertThat(response.amount()).isEqualTo(23000);
        verify(paymentOrderDao).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getAmount()).isEqualTo(23000);
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(PaymentOrderStatus.PENDING);
    }

    @DisplayName("과거 스케줄은 결제 주문 생성 전에 차단한다.")
    @Test
    void createOrderPastSchedule() {
        Member member = member(1L);
        Schedule schedule = schedule(10L, 23000, LocalDate.now().minusDays(1), LocalTime.of(10, 0));
        PaymentOrderRequest request = new PaymentOrderRequest(schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);

        assertThatThrownBy(() -> paymentService.createOrder(request, member, "http://localhost:8080"))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAST_RESERVATION);

        verify(paymentOrderDao, never()).save(any(PaymentOrder.class));
        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
    }

    @DisplayName("본인 중복 예약은 결제 주문 생성 전에 차단한다.")
    @Test
    void createOrderDuplicateReservation() {
        Member member = member(1L);
        Schedule schedule = schedule(10L, 23000);
        PaymentOrderRequest request = new PaymentOrderRequest(schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())).willReturn(true);

        assertThatThrownBy(() -> paymentService.createOrder(request, member, "http://localhost:8080"))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.DUPLICATE_RESERVATION);

        verify(paymentOrderDao, never()).save(any(PaymentOrder.class));
        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
    }

    @DisplayName("이미 예약된 슬롯은 결제 주문 생성 전에 차단한다.")
    @Test
    void createOrderSlotUnavailable() {
        Member member = member(1L);
        Schedule schedule = schedule(10L, 23000);
        PaymentOrderRequest request = new PaymentOrderRequest(schedule.getDate(), 1L, 1L);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(reservationDao.existByMemberIdAndScheduleId(member.getId(), schedule.getId())).willReturn(false);
        given(reservationDao.countReservationByScheduleId(schedule.getId())).willReturn(1);

        assertThatThrownBy(() -> paymentService.createOrder(request, member, "http://localhost:8080"))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE);

        verify(paymentOrderDao, never()).save(any(PaymentOrder.class));
        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
    }

    @DisplayName("콜백 amount가 저장된 주문 금액과 다르면 승인 API를 호출하지 않는다.")
    @Test
    void confirmAmountMismatchBeforeGateway() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", order.getOrderId(), 1000))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAYMENT_AMOUNT_MISMATCH);

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
    }

    @DisplayName("승인 전 슬롯이 이미 차 있으면 승인 API를 호출하지 않는다.")
    @Test
    void confirmSlotUnavailableBeforeGateway() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        given(reservationDao.countReservationByScheduleId(order.getScheduleId())).willReturn(1);

        assertThatThrownBy(() -> paymentService.confirm("payment-key", order.getOrderId(), order.getAmount()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE);

        verify(scheduleService).lockById(order.getScheduleId());
        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
    }

    @DisplayName("이미 확정된 같은 orderId/paymentKey 콜백은 멱등 성공으로 응답한다.")
    @Test
    void confirmIdempotentSuccess() {
        PaymentOrder order = confirmedOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));

        PaymentConfirmResponse response = paymentService.confirm(order.getPaymentKey(), order.getOrderId(), order.getAmount());

        assertThat(response.status()).isEqualTo(PaymentOrderStatus.CONFIRMED.name());
        assertThat(response.reservationId()).isEqualTo(order.getReservationId());
        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
    }

    private PaymentOrder pendingOrder(int amount) {
        return order(amount, PaymentOrderStatus.PENDING, null, null);
    }

    private PaymentOrder confirmedOrder(int amount) {
        return order(amount, PaymentOrderStatus.CONFIRMED, "payment-key", 99L);
    }

    private PaymentOrder order(int amount, PaymentOrderStatus status, String paymentKey, Long reservationId) {
        LocalDateTime now = LocalDateTime.now();
        return new PaymentOrder(
                1L,
                "order-123456",
                1L,
                10L,
                amount,
                status,
                paymentKey,
                reservationId,
                null,
                null,
                now,
                now
        );
    }

    private Member member(Long id) {
        return new Member(id, "member" + id, "회원" + id, "password", Role.USER);
    }

    private Schedule schedule(Long id, int price) {
        return schedule(id, price, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
    }

    private Schedule schedule(Long id, int price, LocalDate date, LocalTime time) {
        return new Schedule(
                id,
                new Theme(1L, "테마", "설명", "https://example.com/theme.jpg", price),
                date,
                new ReservationTime(1L, time)
        );
    }
}
