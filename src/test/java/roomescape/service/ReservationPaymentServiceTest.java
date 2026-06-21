package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.net.SocketTimeoutException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationPaymentDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.ReservationPayment;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.payment.OrderIdGenerator;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConfirmUnknownException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationTimeNotFoundException;

@ExtendWith(MockitoExtension.class)
class ReservationPaymentServiceTest {

    @Mock private ReservationDao reservationDao;
    @Mock private ReservationPaymentDao reservationPaymentDao;
    @Mock private ReservationTimeDao reservationTimeDao;
    @Mock private ThemeDao themeDao;
    @Mock private OrderIdGenerator orderIdGenerator;
    @Mock private PaymentGateway paymentGateway;
    @Mock private Clock clock;

    private ReservationPaymentService reservationPaymentService;

    private final ReservationTime sampleTime = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme sampleTheme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");
    private final LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 14, 12, 0);

    @BeforeEach
    void setUp() {
        reservationPaymentService = new ReservationPaymentService(
                reservationDao,
                reservationPaymentDao,
                reservationTimeDao,
                themeDao,
                orderIdGenerator,
                paymentGateway,
                clock,
                10000L
        );
    }

    @Test
    void prepare_예약_결제_대기_주문을_저장한다() {
        fixClock();
        LocalDate date = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).willReturn(false);
        given(reservationPaymentDao.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).willReturn(false);
        given(orderIdGenerator.generate()).willReturn("order_123456");
        given(reservationPaymentDao.save(any(ReservationPayment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ReservationPayment payment = reservationPaymentService.prepare("브라운", date, 1L, 1L);

        assertThat(payment.getOrderId()).isEqualTo("order_123456");
        assertThat(payment.getAmount()).isEqualTo(10000L);
        assertThat(payment.getReservation().getName()).isEqualTo("브라운");
        then(reservationPaymentDao).should().save(any(ReservationPayment.class));
    }

    @Test
    void prepare_이미_확정_예약된_슬롯이면_예외() {
        LocalDate date = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).willReturn(true);

        assertThatThrownBy(() -> reservationPaymentService.prepare("브라운", date, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");

        then(reservationPaymentDao).should(never()).save(any());
    }

    @Test
    void prepare_이미_결제_대기중인_슬롯이면_예외() {
        LocalDate date = fixedNow.toLocalDate().plusDays(1);
        given(reservationTimeDao.findById(1L)).willReturn(Optional.of(sampleTime));
        given(themeDao.findById(1L)).willReturn(Optional.of(sampleTheme));
        given(reservationDao.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).willReturn(false);
        given(reservationPaymentDao.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).willReturn(true);

        assertThatThrownBy(() -> reservationPaymentService.prepare("브라운", date, 1L, 1L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");

        then(reservationPaymentDao).should(never()).save(any());
    }

    @Test
    void prepare_존재하지_않는_시간이면_예외() {
        given(reservationTimeDao.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationPaymentService.prepare("브라운", fixedNow.toLocalDate().plusDays(1), 99L, 1L))
                .isInstanceOf(ReservationTimeNotFoundException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");

        then(reservationPaymentDao).should(never()).save(any());
    }

    @Test
    void confirm_저장된_금액과_콜백_금액이_다르면_승인_호출_전에_차단한다() {
        LocalDate date = fixedNow.toLocalDate().plusDays(1);
        ReservationPayment payment = payment("order_123456", 10000L, date);
        given(reservationPaymentDao.findByOrderId("order_123456")).willReturn(Optional.of(payment));

        assertThatThrownBy(() -> reservationPaymentService.confirm("payment_key", "order_123456", 9000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        then(paymentGateway).should(never()).confirm(any());
        then(reservationPaymentDao).should(never()).updatePaymentKey(any(), any());
        then(reservationDao).should(never()).save(any());
    }

    @Test
    void confirm_금액이_일치하면_결제를_승인하고_예약을_확정한다() {
        LocalDate date = fixedNow.toLocalDate().plusDays(1);
        ReservationPayment payment = payment("order_123456", 10000L, date);
        given(reservationPaymentDao.findByOrderId("order_123456")).willReturn(Optional.of(payment));
        given(paymentGateway.confirm(new PaymentConfirmation(
                "payment_key", "order_123456", 10000L, payment.getIdempotencyKey())))
                .willReturn(new PaymentResult("payment_key", "order_123456", "DONE", 10000L));
        given(reservationDao.save(payment.getReservation())).willReturn(payment.getReservation());

        var reservation = reservationPaymentService.confirm("payment_key", "order_123456", 10000L);

        assertThat(reservation.getName()).isEqualTo("브라운");
        then(paymentGateway).should().confirm(new PaymentConfirmation(
                "payment_key", "order_123456", 10000L, payment.getIdempotencyKey()));
        then(reservationPaymentDao).should().markConfirmed("order_123456", "payment_key");
        then(reservationDao).should().save(payment.getReservation());
    }

    @Test
    void confirm_readTimeout이면_확인_필요_상태로_저장한다() {
        LocalDate date = fixedNow.toLocalDate().plusDays(1);
        ReservationPayment payment = payment("order_123456", 10000L, date);
        PaymentConfirmUnknownException exception = new PaymentConfirmUnknownException(
                "토스 결제 승인 응답을 받지 못했습니다.",
                new SocketTimeoutException("Read timed out")
        );
        given(reservationPaymentDao.findByOrderId("order_123456")).willReturn(Optional.of(payment));
        given(paymentGateway.confirm(new PaymentConfirmation(
                "payment_key", "order_123456", 10000L, payment.getIdempotencyKey())))
                .willThrow(exception);

        assertThatThrownBy(() -> reservationPaymentService.confirm("payment_key", "order_123456", 10000L))
                .isSameAs(exception);

        then(reservationPaymentDao).should()
                .markConfirmUnknown("order_123456", "CONFIRM_TIMEOUT", "토스 결제 승인 응답을 받지 못했습니다.");
        then(reservationDao).should(never()).save(any());
    }

    @Test
    void fail_orderId가_있으면_결제_대기를_삭제한다() {
        var failure = reservationPaymentService.fail("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", "order_123456");

        assertThat(failure.code()).isEqualTo("PAY_PROCESS_CANCELED");
        assertThat(failure.message()).isEqualTo("사용자가 결제를 취소했습니다.");
        assertThat(failure.orderId()).isEqualTo("order_123456");
        then(reservationPaymentDao).should().deleteByOrderId("order_123456");
    }

    @Test
    void fail_orderId가_없어도_NPE가_나지_않고_삭제하지_않는다() {
        var failure = reservationPaymentService.fail("PAY_PROCESS_CANCELED", "사용자가 결제를 취소했습니다.", null);

        assertThat(failure.code()).isEqualTo("PAY_PROCESS_CANCELED");
        assertThat(failure.orderId()).isNull();
        then(reservationPaymentDao).should(never()).deleteByOrderId(any());
    }

    private void fixClock() {
        given(clock.getZone()).willReturn(ZoneOffset.UTC);
        given(clock.instant()).willReturn(fixedNow.toInstant(ZoneOffset.UTC));
    }

    private ReservationPayment payment(String orderId, long amount, LocalDate date) {
        return new ReservationPayment(
                1L,
                orderId,
                amount,
                null,
                new roomescape.domain.Reservation("브라운", date, fixedNow, sampleTime, sampleTheme)
        );
    }
}
