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
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;
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

    private Reservation pendingReservation(String orderId, Long amount) {
        return fakeReservationRepository.save(
                new Reservation(null, "브라운", 1L, DATE, TIME, THEME, PendingStatus.getInstance(), orderId, amount)
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
}
