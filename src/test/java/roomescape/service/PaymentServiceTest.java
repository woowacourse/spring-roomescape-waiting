package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
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

import roomescape.controller.dto.payment.PaymentOrderRequest;
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
import roomescape.payment.PaymentIdempotencyKeyGenerator;
import roomescape.payment.PaymentOrder;
import roomescape.payment.PaymentOrderIdGenerator;
import roomescape.payment.PaymentOrderStatus;
import roomescape.payment.PaymentResult;
import roomescape.repository.PaymentOrderDao;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentOrderDao paymentOrderDao;

    @Mock
    private PaymentOrderStatusService paymentOrderStatusService;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentOrderIdGenerator orderIdGenerator;

    @Mock
    private PaymentIdempotencyKeyGenerator idempotencyKeyGenerator;

    private final PaymentCheckoutProperties checkoutProperties = new PaymentCheckoutProperties("test_ck_123");

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentOrderDao,
                paymentOrderStatusService,
                scheduleService,
                reservationService,
                paymentGateway,
                orderIdGenerator,
                idempotencyKeyGenerator,
                checkoutProperties
        );
    }

    @DisplayName("결제 주문 금액은 테마 가격으로 저장한다.")
    @Test
    void createOrderUsesThemePrice() {
        Member member = member(1L);
        Schedule schedule = schedule(10L, 23000);
        PaymentOrderRequest request = request(schedule);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(paymentOrderDao.findReusableByMemberIdAndScheduleId(member.getId(), schedule.getId()))
                .willReturn(Optional.empty());
        given(orderIdGenerator.generate()).willReturn("order-123456");
        given(idempotencyKeyGenerator.generate()).willReturn("fixed-idempotency-key");
        ArgumentCaptor<PaymentOrder> orderCaptor = ArgumentCaptor.forClass(PaymentOrder.class);

        PaymentOrder response = paymentService.createOrder(request, member);

        assertThat(response.getOrderId()).isEqualTo("order-123456");
        assertThat(response.getAmount()).isEqualTo(23000);
        verify(reservationService).validatePaymentOrderCreatable(eq(member), eq(schedule), any(LocalDateTime.class));
        verify(paymentOrderDao).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getAmount()).isEqualTo(23000);
        assertThat(orderCaptor.getValue().getIdempotencyKey()).isEqualTo("fixed-idempotency-key");
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(PaymentOrderStatus.PENDING);
    }

    @DisplayName("진행 중인 결제 주문이 있으면 새 멱등키를 만들지 않고 기존 주문을 반환한다.")
    @Test
    void createOrderReusesPendingOrder() {
        Member member = member(1L);
        Schedule schedule = schedule(10L, 23000);
        PaymentOrderRequest request = request(schedule);
        PaymentOrder existingOrder = pendingOrder(23000);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(paymentOrderDao.findReusableByMemberIdAndScheduleId(member.getId(), schedule.getId()))
                .willReturn(Optional.of(existingOrder));

        PaymentOrder response = paymentService.createOrder(request, member);

        assertThat(response).isEqualTo(existingOrder);
        verify(orderIdGenerator, never()).generate();
        verify(idempotencyKeyGenerator, never()).generate();
        verify(paymentOrderDao, never()).save(any(PaymentOrder.class));
    }

    @DisplayName("확인 필요 주문이 있으면 새 주문을 만들지 않고 같은 멱등키를 재사용한다.")
    @Test
    void createOrderReusesConfirmUnknownOrder() {
        Member member = member(1L);
        Schedule schedule = schedule(10L, 23000);
        PaymentOrderRequest request = request(schedule);
        PaymentOrder existingOrder = order(23000, PaymentOrderStatus.CONFIRM_UNKNOWN, null, null);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        given(paymentOrderDao.findReusableByMemberIdAndScheduleId(member.getId(), schedule.getId()))
                .willReturn(Optional.of(existingOrder));

        PaymentOrder response = paymentService.createOrder(request, member);

        assertThat(response.getOrderId()).isEqualTo(existingOrder.getOrderId());
        assertThat(response.getIdempotencyKey()).isEqualTo(existingOrder.getIdempotencyKey());
        verify(orderIdGenerator, never()).generate();
        verify(idempotencyKeyGenerator, never()).generate();
        verify(paymentOrderDao, never()).save(any(PaymentOrder.class));
    }

    @DisplayName("과거 스케줄은 결제 주문 생성 전에 차단한다.")
    @Test
    void createOrderPastSchedule() {
        Member member = member(1L);
        Schedule schedule = schedule(10L, 23000, LocalDate.now().minusDays(1), LocalTime.of(10, 0));
        PaymentOrderRequest request = request(schedule);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        willThrow(new RoomescapeException(DomainErrorCode.PAST_RESERVATION, "과거 시각으로는 결제를 시작할 수 없습니다."))
                .given(reservationService)
                .validatePaymentOrderCreatable(eq(member), eq(schedule), any(LocalDateTime.class));

        assertThatThrownBy(() -> paymentService.createOrder(request, member))
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
        PaymentOrderRequest request = request(schedule);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        willThrow(new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 해당 스케줄에 본인의 예약이 존재합니다."))
                .given(reservationService)
                .validatePaymentOrderCreatable(eq(member), eq(schedule), any(LocalDateTime.class));

        assertThatThrownBy(() -> paymentService.createOrder(request, member))
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
        PaymentOrderRequest request = request(schedule);
        given(scheduleService.getOrCreateScheduleForUpdate(request.date(), request.timeId(), request.themeId()))
                .willReturn(schedule);
        willThrow(new RoomescapeException(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE, "이미 예약된 슬롯은 결제할 수 없습니다."))
                .given(reservationService)
                .validatePaymentOrderCreatable(eq(member), eq(schedule), any(LocalDateTime.class));

        assertThatThrownBy(() -> paymentService.createOrder(request, member))
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
        verify(reservationService, never()).validateBeforeConfirm(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(reservationService, never()).saveConfirmedReservationByPayment(anyLong(), anyLong());
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("승인 전 슬롯이 이미 차 있으면 승인 API를 호출하지 않는다.")
    @Test
    void confirmSlotUnavailableBeforeGateway() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        willThrow(new RoomescapeException(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE, "이미 예약된 슬롯은 결제할 수 없습니다."))
                .given(reservationService)
                .validateBeforeConfirm(eq(order.getMemberId()), eq(order.getScheduleId()), any(LocalDateTime.class));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", order.getOrderId(), order.getAmount()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE);

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
        verify(reservationService, never()).saveConfirmedReservationByPayment(anyLong(), anyLong());
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("승인 전 본인 중복 예약이면 승인 API를 호출하지 않는다.")
    @Test
    void confirmDuplicateReservationBeforeGateway() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        willThrow(new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 해당 스케줄에 본인의 예약이 존재합니다."))
                .given(reservationService)
                .validateBeforeConfirm(eq(order.getMemberId()), eq(order.getScheduleId()), any(LocalDateTime.class));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", order.getOrderId(), order.getAmount()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.DUPLICATE_RESERVATION);

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
        verify(reservationService, never()).saveConfirmedReservationByPayment(anyLong(), anyLong());
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("승인 전 과거 스케줄이면 승인 API를 호출하지 않는다.")
    @Test
    void confirmPastScheduleBeforeGateway() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        willThrow(new RoomescapeException(DomainErrorCode.PAST_RESERVATION, "과거 시각으로는 결제를 시작할 수 없습니다."))
                .given(reservationService)
                .validateBeforeConfirm(eq(order.getMemberId()), eq(order.getScheduleId()), any(LocalDateTime.class));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", order.getOrderId(), order.getAmount()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAST_RESERVATION);

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
        verify(reservationService, never()).saveConfirmedReservationByPayment(anyLong(), anyLong());
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("주문 정보가 없으면 승인 API를 호출하지 않는다.")
    @Test
    void confirmOrderNotFound() {
        given(paymentOrderDao.findByOrderId("missing-order")).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "missing-order", 23000))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER);

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
        verify(reservationService, never()).validateBeforeConfirm(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(reservationService, never()).saveConfirmedReservationByPayment(anyLong(), anyLong());
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("이미 확정된 같은 orderId/paymentKey 콜백은 멱등 성공으로 응답한다.")
    @Test
    void confirmIdempotentSuccess() {
        PaymentOrder order = confirmedOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));

        paymentService.confirm(order.getPaymentKey(), order.getOrderId(), order.getAmount());

        verify(paymentGateway, never()).confirm(any(PaymentConfirmation.class));
        verify(reservationService, never()).validateBeforeConfirm(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(reservationService, never()).saveConfirmedReservationByPayment(anyLong(), anyLong());
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("승인 API가 실패하면 예약 확정과 주문 확정을 하지 않는다.")
    @Test
    void confirmGatewayFailure() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .willThrow(new RoomescapeException(DomainErrorCode.PAYMENT_REJECTED, "카드 결제가 거절되었습니다."));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", order.getOrderId(), order.getAmount()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAYMENT_REJECTED);

        verify(reservationService).validateBeforeConfirm(eq(order.getMemberId()), eq(order.getScheduleId()), any(LocalDateTime.class));
        verify(reservationService, never()).saveConfirmedReservationByPayment(anyLong(), anyLong());
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("승인 응답 확인이 불명확하면 주문을 확인 필요 상태로 표시한다.")
    @Test
    void confirmUnknownGatewayResult() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .willThrow(new RoomescapeException(DomainErrorCode.PAYMENT_CONFIRM_UNKNOWN, "결제 승인 응답을 받지 못했습니다."));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", order.getOrderId(), order.getAmount()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAYMENT_CONFIRM_UNKNOWN);

        verify(paymentOrderStatusService).markConfirmUnknown(
                eq(order.getOrderId()),
                eq(DomainErrorCode.PAYMENT_CONFIRM_UNKNOWN.name()),
                eq("결제 승인 응답을 받지 못했습니다.")
        );
        verify(reservationService, never()).saveConfirmedReservationByPayment(anyLong(), anyLong());
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("확인 필요 상태의 같은 주문은 저장된 멱등키로 승인 재시도를 수행한다.")
    @Test
    void retryConfirmUnknownOrder() {
        PaymentOrder order = order(23000, PaymentOrderStatus.CONFIRM_UNKNOWN, null, null);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .willReturn(new PaymentResult("payment-key", order.getOrderId(), order.getAmount(), "DONE"));
        given(reservationService.saveConfirmedReservationByPayment(order.getScheduleId(), order.getMemberId()))
                .willReturn(99L);
        ArgumentCaptor<PaymentConfirmation> confirmationCaptor = ArgumentCaptor.forClass(PaymentConfirmation.class);

        paymentService.confirm("payment-key", order.getOrderId(), order.getAmount());

        verify(paymentGateway).confirm(confirmationCaptor.capture());
        assertThat(confirmationCaptor.getValue().idempotencyKey()).isEqualTo(order.getIdempotencyKey());
        verify(paymentOrderDao).confirm(eq(order.getOrderId()), eq("payment-key"), eq(99L), any(LocalDateTime.class));
    }

    @DisplayName("승인 API 성공 후 확정 예약을 저장하고 주문을 CONFIRMED로 변경한다.")
    @Test
    void confirmSuccess() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .willReturn(new PaymentResult("payment-key", order.getOrderId(), order.getAmount(), "DONE"));
        given(reservationService.saveConfirmedReservationByPayment(order.getScheduleId(), order.getMemberId()))
                .willReturn(99L);
        ArgumentCaptor<PaymentConfirmation> confirmationCaptor = ArgumentCaptor.forClass(PaymentConfirmation.class);

        paymentService.confirm("payment-key", order.getOrderId(), order.getAmount());

        verify(reservationService).validateBeforeConfirm(eq(order.getMemberId()), eq(order.getScheduleId()), any(LocalDateTime.class));
        verify(paymentGateway).confirm(confirmationCaptor.capture());
        PaymentConfirmation confirmation = confirmationCaptor.getValue();
        assertThat(confirmation.paymentKey()).isEqualTo("payment-key");
        assertThat(confirmation.orderId()).isEqualTo(order.getOrderId());
        assertThat(confirmation.amount()).isEqualTo(order.getAmount());
        assertThat(confirmation.idempotencyKey()).isEqualTo(order.getIdempotencyKey());
        verify(paymentOrderDao).confirm(eq(order.getOrderId()), eq("payment-key"), eq(99L), any(LocalDateTime.class));
    }

    @DisplayName("승인 후 확정 예약 저장에 실패하면 주문을 CONFIRMED로 변경하지 않는다.")
    @Test
    void confirmReservationSaveFailureAfterGateway() {
        PaymentOrder order = pendingOrder(23000);
        given(paymentOrderDao.findByOrderId(order.getOrderId())).willReturn(Optional.of(order));
        given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                .willReturn(new PaymentResult("payment-key", order.getOrderId(), order.getAmount(), "DONE"));
        given(reservationService.saveConfirmedReservationByPayment(order.getScheduleId(), order.getMemberId()))
                .willThrow(new RoomescapeException(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE, "이미 예약된 슬롯은 결제할 수 없습니다."));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", order.getOrderId(), order.getAmount()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.PAYMENT_SLOT_UNAVAILABLE);

        verify(paymentGateway).confirm(any(PaymentConfirmation.class));
        verify(paymentOrderDao, never()).confirm(any(), any(), any(), any(LocalDateTime.class));
    }

    @DisplayName("failUrl에 orderId가 있으면 결제 주문 실패 정보를 저장한다.")
    @Test
    void failWithOrderId() {
        paymentService.fail("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", "order-123456");

        verify(paymentOrderDao).fail(eq("order-123456"), eq("PAY_PROCESS_CANCELED"), eq("사용자가 결제를 취소했습니다."), any(LocalDateTime.class));
    }

    @DisplayName("failUrl에 orderId가 없으면 아무 작업도 하지 않는다.")
    @Test
    void failWithoutOrderId() {
        paymentService.fail("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", null);
        paymentService.fail("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", " ");

        verify(paymentOrderDao, never()).fail(any(), any(), any(), any(LocalDateTime.class));
    }

    private PaymentOrder pendingOrder(int amount) {
        return PaymentOrder.pending(
                "order-123456",
                1L,
                10L,
                amount,
                "fixed-idempotency-key",
                LocalDateTime.now()
        );
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
                "fixed-idempotency-key",
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

    private PaymentOrderRequest request(Schedule schedule) {
        return new PaymentOrderRequest(schedule.getDate(), 1L, 1L);
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
