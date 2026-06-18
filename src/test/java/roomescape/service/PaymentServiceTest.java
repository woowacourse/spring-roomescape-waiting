package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.TestClockConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.domain.payment.PaymentOrder;
import roomescape.dto.response.ReservationPaymentResponse;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.fixture.Fixtures;
import roomescape.infrastructure.payment.TossPaymentException;
import roomescape.repository.fake.FakePaymentOrderRepository;
import roomescape.repository.fake.FakeReservationRepository;
import roomescape.repository.fake.FakeReservationTimeRepository;
import roomescape.repository.fake.FakeStoreRepository;
import roomescape.repository.fake.FakeThemeRepository;
import roomescape.repository.fake.FakeUserRepository;
import roomescape.service.payment.FakePaymentGateway;
import roomescape.service.payment.FixedIdempotencyKeyGenerator;
import roomescape.service.payment.FixedOrderIdGenerator;

class PaymentServiceTest {

    private FakeReservationRepository reservationRepository;
    private FakePaymentOrderRepository paymentOrderRepository;
    private FakePaymentGateway paymentGateway;
    private ReservationService reservationService;
    private PaymentService paymentService;
    private FakeThemeRepository themeRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeUserRepository userRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        paymentOrderRepository = new FakePaymentOrderRepository();
        paymentGateway = new FakePaymentGateway();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository(reservationRepository);
        userRepository = new FakeUserRepository();
        FakeStoreRepository storeRepository = new FakeStoreRepository();
        storeRepository.save(Fixtures.store("매장"));
        reservationService = new ReservationService(reservationRepository, themeRepository, reservationTimeRepository,
                userRepository, storeRepository, paymentOrderRepository, new FixedOrderIdGenerator("order_123456"),
                new FixedIdempotencyKeyGenerator("idempotency-key-123"),
                new TestClockConfig().timeProvider());
        paymentService = new PaymentService(paymentOrderRepository, reservationRepository, paymentGateway);
    }

    @Test
    void confirmPayment_저장된_금액과_콜백_amount가_다르면_승인_호출_전에_예외() {
        ReservationPaymentResponse created = createPendingReservation(37_000L);

        assertThatThrownBy(() -> paymentService.confirmPayment("payment_key", created.orderId(), 1_000L))
                .isInstanceOf(PaymentAmountMismatchException.class)
                .hasMessage("결제 금액이 주문 금액과 일치하지 않습니다.");

        assertThat(paymentGateway.requestedConfirmation()).isNull();
        assertThat(paymentOrderRepository.findByOrderId(created.orderId()).orElseThrow().getPaymentKey()).isNull();
        assertThat(reservationRepository.findById(created.reservationId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PAYMENT_PENDING);
    }

    @Test
    void confirmPayment_금액이_일치하면_승인하고_paymentKey를_저장한_뒤_예약을_확정한다() {
        ReservationPaymentResponse created = createPendingReservation(37_000L);

        paymentService.confirmPayment("payment_key", created.orderId(), 37_000L);

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(created.orderId()).orElseThrow();
        assertThat(paymentOrder.getPaymentKey()).isEqualTo("payment_key");
        assertThat(paymentGateway.requestedConfirmation().orderId()).isEqualTo(created.orderId());
        assertThat(paymentGateway.requestedConfirmation().amount()).isEqualTo(37_000L);
        assertThat(paymentGateway.requestedConfirmation().idempotencyKey()).isEqualTo("idempotency-key-123");
        Reservation reservation = reservationRepository.findById(created.reservationId()).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void confirmPayment_승인_응답을_받지_못하면_paymentKey를_저장하고_확인_필요_상태로_조회되게_한다() {
        ReservationPaymentResponse created = createPendingReservation(37_000L);
        paymentGateway.failWithConfirmationUnknown();

        assertThatThrownBy(() -> paymentService.confirmPayment("payment_key", created.orderId(), 37_000L))
                .isInstanceOf(TossPaymentException.ConfirmationUnknown.class);

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(created.orderId()).orElseThrow();
        assertThat(paymentOrder.getPaymentKey()).isEqualTo("payment_key");
        assertThat(reservationRepository.findById(created.reservationId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PAYMENT_PENDING);
    }

    @Test
    void failPayment_결제_대기_주문이면_실패_상태로_변경한다() {
        ReservationPaymentResponse created = createPendingReservation(37_000L);

        paymentService.failPayment(created.orderId());

        assertThat(paymentOrderRepository.findByOrderId(created.orderId())).isPresent();
        assertThat(reservationRepository.findById(created.reservationId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.PAYMENT_FAILED);
    }

    @Test
    void failPayment_orderId가_없으면_아무것도_하지_않는다() {
        ReservationPaymentResponse created = createPendingReservation(37_000L);

        paymentService.failPayment(null);

        assertThat(paymentOrderRepository.findByOrderId(created.orderId())).isPresent();
        assertThat(reservationRepository.findById(created.reservationId())).isPresent();
    }

    @Test
    void failPayment_이미_확정된_예약은_삭제하지_않는다() {
        ReservationPaymentResponse created = createPendingReservation(37_000L);
        paymentService.confirmPayment("payment_key", created.orderId(), 37_000L);

        paymentService.failPayment(created.orderId());

        assertThat(paymentOrderRepository.findByOrderId(created.orderId())).isPresent();
        assertThat(reservationRepository.findById(created.reservationId())).isPresent();
    }

    private ReservationPaymentResponse createPendingReservation(long amount) {
        Long userId = userRepository.save(Fixtures.member("브라운"));
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "https://thumbnail.url"));
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        return reservationService.createReservation(
                Fixtures.createCommand(userId, themeId, LocalDate.of(2026, 5, 8), timeId, amount));
    }
}
