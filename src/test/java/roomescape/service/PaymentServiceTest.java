package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationOrder.OrderStatus;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;
import roomescape.dto.payment.PaymentConfirmRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.PaymentAmountMismatchException;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.exception.PaymentException.PaymentNotFoundException;
import roomescape.exception.PaymentException.PaymentResultUnknownException;
import roomescape.fake.FakePaymentGateway;
import roomescape.fake.FakeReservationOrderRepository;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeTransactionManager;

class PaymentServiceTest {

    private FakeReservationRepository reservationRepository;
    private FakeReservationOrderRepository orderRepository;
    private FakePaymentGateway paymentGateway;
    private PaymentService paymentService;

    private static final ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
    private static final Theme theme = new Theme(2L, "test", "설명", "url");

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        orderRepository = new FakeReservationOrderRepository();
        paymentGateway = new FakePaymentGateway();
        ReservationOrderService orderService = new ReservationOrderService(orderRepository);
        TransactionTemplate transactionTemplate = new TransactionTemplate(new FakeTransactionManager());
        paymentService = new PaymentService(transactionTemplate, orderService, reservationRepository, paymentGateway);
    }

    private void reservation(long id) {
        Slot slot = Slot.restore(id, LocalDate.now().plusDays(1), time, theme);
        reservationRepository.save(Reservation.restore(id, slot, "테스트", LocalDateTime.now(), false));
    }

    @Test
    void 결제_승인에_성공하면_paymentKey가_저장되고_예약이_결제완료_처리된다() {
        reservation(1L);
        orderRepository.save(ReservationOrder.restore("order-1", 10000, null, 1L));
        paymentGateway.setResult(
                new PaymentResult("pk_test", "order-1", 10000, PaymentStatus.APPROVED, OffsetDateTime.now()));

        ReservationResponse response = paymentService.confirm(new PaymentConfirmRequest("pk_test", "order-1", 10000));

        assertThat(paymentGateway.isCalled()).isTrue();
        assertThat(orderRepository.findById("order-1")).get()
                .extracting(ReservationOrder::getPaymentKey).isEqualTo("pk_test");
        assertThat(reservationRepository.findReservationById(1L)).get()
                .extracting(Reservation::isPaid).isEqualTo(true);
        assertThat(response.paid()).isTrue();
        assertThat(response.name()).isEqualTo("테스트");
    }

    @Test
    void 주문_금액과_일치하지_않으면_승인_호출_전에_예외가_발생한다() {
        reservation(1L);
        orderRepository.save(ReservationOrder.restore("order-1", 10000, null, 1L));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest("pk_test", "order-1", 20000)))
                .isInstanceOf(PaymentAmountMismatchException.class);

        assertThat(paymentGateway.isCalled()).isFalse();
    }

    @Test
    void 예약이_없으면_승인_호출_전에_차단된다() {
        orderRepository.save(ReservationOrder.restore("order-1", 10000, null, 1L));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest("pk_test", "order-1", 10000)))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(paymentGateway.isCalled()).isFalse();
    }

    @Test
    void 이미_결제된_예약이면_승인_호출_전에_차단된다() {
        Slot slot = Slot.restore(1L, LocalDate.now().plusDays(1), time, theme);
        reservationRepository.save(Reservation.restore(1L, slot, "테스트", LocalDateTime.now(), true));
        orderRepository.save(ReservationOrder.restore("order-1", 10000, null, 1L));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest("pk_test", "order-1", 10000)))
                .isInstanceOf(AlreadyProcessedException.class);

        assertThat(paymentGateway.isCalled()).isFalse();
    }

    @Test
    void 존재하지_않는_주문이면_승인_호출_전에_예외가_발생한다() {
        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest("pk_test", "no-order", 10000)))
                .isInstanceOf(PaymentNotFoundException.class);

        assertThat(paymentGateway.isCalled()).isFalse();
    }

    @Test
    void 결과가_불명확하면_주문을_확인필요로_기록하고_예외를_던진다() {
        reservation(1L);
        orderRepository.save(ReservationOrder.restore("order-1", 10000, null, 1L));
        paymentGateway.setException(new PaymentResultUnknownException("결제 결과를 확인하지 못했습니다."));

        assertThatThrownBy(() -> paymentService.confirm(new PaymentConfirmRequest("pk_test", "order-1", 10000)))
                .isInstanceOf(PaymentResultUnknownException.class);

        assertThat(orderRepository.findById("order-1")).get()
                .extracting(ReservationOrder::getStatus).isEqualTo(OrderStatus.UNKNOWN);
        assertThat(reservationRepository.findReservationById(1L)).get()
                .extracting(Reservation::isPaid).isEqualTo(false);
    }
}
