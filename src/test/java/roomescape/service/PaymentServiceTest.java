package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.controller.dto.ReservationPaymentResponse;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.FakePaymentRepository;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeThemeSlotRepository;

class PaymentServiceTest {

    private static final Theme THEME = new Theme(1L, "테마", "설명", "test.com", 10000L);
    private static final Time TIME = new Time(1L, LocalTime.of(10, 0));
    private static final LocalDate DATE = LocalDate.now().plusDays(1);

    private PaymentService paymentService;
    private FakeReservationRepository fakeReservationRepository;
    private FakePaymentRepository fakePaymentRepository;
    private FakeThemeSlotRepository fakeThemeSlotRepository;

    private final PaymentGateway successGateway = confirmation ->
            new PaymentResult(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());

    @BeforeEach
    void setUp() {
        fakeReservationRepository = new FakeReservationRepository();
        fakePaymentRepository = new FakePaymentRepository();
        fakeThemeSlotRepository = new FakeThemeSlotRepository(fakeReservationRepository);
        paymentService = new PaymentService(successGateway, fakeReservationRepository, fakePaymentRepository, fakeThemeSlotRepository);

        fakeThemeSlotRepository.save(new ThemeSlot(THEME, DATE, TIME, false));
    }

    private Reservation pendingReservation(String name, String orderId, Long amount) {
        return fakeReservationRepository.save(
                new Reservation(null, name, 1L, DATE, TIME, THEME, PendingStatus.getInstance(), orderId, amount)
        );
    }

    private Reservation pendingReservation(String orderId, Long amount) {
        return pendingReservation("브라운", orderId, amount);
    }

    private Reservation confirmedReservation(String name, String orderId, Long amount) {
        return fakeReservationRepository.save(
                new Reservation(null, name, 1L, DATE, TIME, THEME, ConfirmedStatus.getInstance(), orderId, amount)
        );
    }

    @Test
    @DisplayName("결제 승인이 성공하면 예약이 CONFIRMED 상태가 되고 슬롯이 예약됨으로 표시된다.")
    void confirmPayment_success_confirmsReservationAndMarkSlotReserved() {
        Reservation reservation = pendingReservation("order-123", 10000L);
        PaymentConfirmation confirmation = new PaymentConfirmation("pay-key", "order-123", 10000L);

        Payment payment = paymentService.confirmPayment(confirmation);

        assertThat(fakeReservationRepository.findById(reservation.getId()).get().isConfirmed()).isTrue();
        assertThat(fakeThemeSlotRepository.findById(1L).get().isReserved()).isTrue();
        assertThat(payment.getOrderId()).isEqualTo("order-123");
    }

    @Test
    @DisplayName("결제 금액이 주문 금액과 다르면 PAYMENT_AMOUNT_MISMATCH 예외가 발생한다.")
    void confirmPayment_amountMismatch_throwsException() {
        pendingReservation("order-123", 10000L);
        PaymentConfirmation confirmation = new PaymentConfirmation("pay-key", "order-123", 9999L);

        assertThatThrownBy(() -> paymentService.confirmPayment(confirmation))
                .isInstanceOf(CustomException.class)
                .hasMessage("결제 금액이 주문 금액과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("금액 불일치 시 PaymentGateway.confirm()은 호출되지 않는다.")
    void confirmPayment_amountMismatch_gatewayNotCalled() {
        List<PaymentConfirmation> called = new ArrayList<>();
        PaymentGateway trackingGateway = confirmation -> {
            called.add(confirmation);
            return new PaymentResult(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        };
        paymentService = new PaymentService(trackingGateway, fakeReservationRepository, fakePaymentRepository, fakeThemeSlotRepository);

        pendingReservation("order-123", 10000L);
        PaymentConfirmation confirmation = new PaymentConfirmation("pay-key", "order-123", 9999L);

        assertThatThrownBy(() -> paymentService.confirmPayment(confirmation)).isInstanceOf(CustomException.class);
        assertThat(called).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 orderId로 결제 승인 시 RESERVATION_NOT_FOUND 예외가 발생한다.")
    void confirmPayment_orderIdNotFound_throwsException() {
        PaymentConfirmation confirmation = new PaymentConfirmation("pay-key", "nonexistent", 10000L);

        assertThatThrownBy(() -> paymentService.confirmPayment(confirmation))
                .isInstanceOf(CustomException.class)
                .hasMessage("예약이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("read timeout 발생 시 예약은 PENDING을 유지하고 UNCERTAIN Payment가 저장된다.")
    void confirmPayment_readTimeout_savesUncertainPaymentAndKeepsPending() {
        PaymentGateway timeoutGateway = confirmation -> {
            throw new CustomException(ErrorCode.PAYMENT_READ_TIMEOUT);
        };
        paymentService = new PaymentService(timeoutGateway, fakeReservationRepository, fakePaymentRepository, fakeThemeSlotRepository);

        Reservation reservation = pendingReservation("order-123", 10000L);
        PaymentConfirmation confirmation = new PaymentConfirmation("pay-key", "order-123", 10000L);

        Payment payment = paymentService.confirmPayment(confirmation);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.UNCERTAIN);
        assertThat(payment.getPaymentKey()).isNull();
        assertThat(fakeReservationRepository.findById(reservation.getId()).get().isConfirmed()).isFalse();
    }

    @Test
    @DisplayName("read timeout 이외의 결제 예외는 그대로 전파된다.")
    void confirmPayment_connectionTimeout_propagatesException() {
        PaymentGateway connectionFailGateway = confirmation -> {
            throw new CustomException(ErrorCode.PAYMENT_CONNECTION_TIMEOUT);
        };
        paymentService = new PaymentService(connectionFailGateway, fakeReservationRepository, fakePaymentRepository, fakeThemeSlotRepository);

        pendingReservation("order-123", 10000L);
        PaymentConfirmation confirmation = new PaymentConfirmation("pay-key", "order-123", 10000L);

        assertThatThrownBy(() -> paymentService.confirmPayment(confirmation))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.PAYMENT_CONNECTION_TIMEOUT.getMessage());
    }

    @Test
    @DisplayName("결제 실패 처리 시 해당 orderId의 예약이 CANCELLED 상태가 된다.")
    void handlePaymentFail_withValidOrderId_cancelsReservation() {
        Reservation reservation = pendingReservation("order-123", 10000L);

        paymentService.handlePaymentFail("order-123");

        assertThat(fakeReservationRepository.findById(reservation.getId()).get().isCancelled()).isTrue();
    }

    @Test
    @DisplayName("orderId가 null이면 결제 실패 처리 시 아무것도 하지 않는다.")
    void handlePaymentFail_withNullOrderId_doesNothing() {
        paymentService.handlePaymentFail(null);
        // 예외 없이 정상 종료
    }

    @Test
    @DisplayName("존재하지 않는 orderId로 결제 실패 처리 시 조용히 무시된다.")
    void handlePaymentFail_withUnknownOrderId_doesNothing() {
        paymentService.handlePaymentFail("nonexistent-order");
        // 예외 없이 정상 종료
    }

    // ── getPaymentHistory ─────────────────────────────────────────

    @Test
    @DisplayName("CONFIRMED payment가 있는 예약은 paymentStatus CONFIRMED와 paymentKey를 포함해 반환한다.")
    void getPaymentHistory_confirmedPayment_returnsConfirmedStatus() {
        Reservation reservation = confirmedReservation("브라운", "order-123", 10000L);
        fakePaymentRepository.save(new Payment(reservation.getId(), "pay-key", "order-123", 10000L, PaymentStatus.CONFIRMED));

        List<ReservationPaymentResponse> history = paymentService.getPaymentHistory("브라운");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).paymentStatus()).isEqualTo("CONFIRMED");
        assertThat(history.get(0).paymentKey()).isEqualTo("pay-key");
        assertThat(history.get(0).orderId()).isEqualTo("order-123");
    }

    @Test
    @DisplayName("UNCERTAIN payment가 있는 예약은 paymentStatus UNCERTAIN이고 paymentKey는 null이다.")
    void getPaymentHistory_uncertainPayment_returnsUncertainStatus() {
        Reservation reservation = pendingReservation("브라운", "order-123", 10000L);
        fakePaymentRepository.save(new Payment(reservation.getId(), null, "order-123", 10000L, PaymentStatus.UNCERTAIN));

        List<ReservationPaymentResponse> history = paymentService.getPaymentHistory("브라운");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).paymentStatus()).isEqualTo("UNCERTAIN");
        assertThat(history.get(0).paymentKey()).isNull();
    }

    @Test
    @DisplayName("payment 기록이 없는 예약은 paymentStatus와 paymentKey가 null이다.")
    void getPaymentHistory_noPayment_returnsNullPaymentStatus() {
        pendingReservation("브라운", "order-123", 10000L);

        List<ReservationPaymentResponse> history = paymentService.getPaymentHistory("브라운");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).paymentStatus()).isNull();
        assertThat(history.get(0).paymentKey()).isNull();
    }

    @Test
    @DisplayName("CONFIRMED/UNCERTAIN/결제없음 예약이 섞여 있어도 전체 목록을 반환한다.")
    void getPaymentHistory_mixedStatuses_returnsAll() {
        Reservation confirmed = confirmedReservation("브라운", "order-1", 10000L);
        fakePaymentRepository.save(new Payment(confirmed.getId(), "pay-key", "order-1", 10000L, PaymentStatus.CONFIRMED));

        Reservation uncertain = pendingReservation("브라운", "order-2", 10000L);
        fakePaymentRepository.save(new Payment(uncertain.getId(), null, "order-2", 10000L, PaymentStatus.UNCERTAIN));

        pendingReservation("브라운", "order-3", 10000L);

        List<ReservationPaymentResponse> history = paymentService.getPaymentHistory("브라운");

        assertThat(history).hasSize(3);
        assertThat(history).extracting(ReservationPaymentResponse::paymentStatus)
                .containsExactlyInAnyOrder("CONFIRMED", "UNCERTAIN", null);
    }

    @Test
    @DisplayName("해당 이름의 예약이 없으면 빈 리스트를 반환한다.")
    void getPaymentHistory_noReservation_returnsEmptyList() {
        List<ReservationPaymentResponse> history = paymentService.getPaymentHistory("없는사람");

        assertThat(history).isEmpty();
    }
}
