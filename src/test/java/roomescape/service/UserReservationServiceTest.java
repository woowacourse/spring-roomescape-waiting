package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingOrder;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentService;
import roomescape.payment.PaymentStatus;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.PaymentOrderRepository;
import roomescape.repository.LockedReservationWriter;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeLockedAction;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.PaymentConfirmCommand;
import roomescape.service.dto.PaymentOrderResult;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationUpdateCommand;
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.service.exception.PastReservationException;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;
import roomescape.service.exception.UnauthorizedReservationException;

@ExtendWith(MockitoExtension.class)
class UserReservationServiceTest {

    private static final ReservationTime VALID_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final ReservationTime ANOTHER_TIME = new ReservationTime(2L, LocalTime.of(11, 0));
    private static final Theme VALID_THEME = new Theme(
            1L,
            "무인도 탈출",
            "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!",
            "https://picsum.photos/seed/roomescape1/800/600.jpg"
    );
    private static final LocalDate FUTURE_DATE = LocalDate.of(2099, 12, 31);
    private static final LocalDate ANOTHER_FUTURE_DATE = LocalDate.of(2099, 11, 30);
    private static final LocalDate PAST_DATE = LocalDate.of(2020, 1, 1);
    private static final String OWNER = "카프카";
    private static final String OTHER = "모아";

    @Mock
    private AdminReservationService reservationService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private LockedReservationWriter reservationWriter;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    @InjectMocks
    private UserReservationService userReservationService;

    private void stubThemeLock(Optional<Theme> lockedTheme) {
        given(reservationRepository.executeWithThemeLock(eq(VALID_THEME.getId()), any()))
                .willAnswer(invocation -> {
                    ThemeLockedAction<Object> action = invocation.getArgument(1);
                    return action.execute(lockedTheme, reservationWriter);
                });
    }

    @Test
    @DisplayName("미래 시점에 주문하면 결제 대기 주문이 저장되고 예약은 아직 생성되지 않는다")
    void 미래_시점_주문은_결제대기로_저장된다() {
        ReservationCreateCommand command = new ReservationCreateCommand(OWNER, FUTURE_DATE, 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
        given(themeRepository.existsById(1L)).willReturn(true);

        PaymentOrderResult result = userReservationService.createOrder(command);

        assertThat(result.orderId()).isNotBlank();
        assertThat(result.amount()).isPositive();
        verify(reservationTimeRepository, times(1)).findById(1L);
        verify(paymentOrderRepository, times(1)).save(any(PaymentOrder.class));
        verifyNoInteractions(reservationService, reservationRepository, paymentService);
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 주문하면 ThemeNotFoundException이 발생하고 주문이 저장되지 않는다")
    void 존재하지_않는_themeId로_주문시_예외가_발생한다() {
        ReservationCreateCommand command = new ReservationCreateCommand(OWNER, FUTURE_DATE, 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));
        given(themeRepository.existsById(1L)).willReturn(false);

        assertThrows(
                ThemeNotFoundException.class,
                () -> userReservationService.createOrder(command)
        );

        verify(themeRepository, times(1)).existsById(1L);
        verifyNoInteractions(reservationService, reservationRepository, paymentService, paymentOrderRepository);
    }

    @Test
    @DisplayName("과거 날짜로 주문하면 PastReservationException이 발생하고 주문이 저장되지 않는다")
    void 과거_날짜_주문시_예외가_발생한다() {
        ReservationCreateCommand command = new ReservationCreateCommand(OWNER, PAST_DATE, 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(VALID_TIME));

        assertThrows(
                PastReservationException.class,
                () -> userReservationService.createOrder(command)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationRepository, paymentService, paymentOrderRepository);
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 주문하면 ReservationTimeNotFoundException이 발생한다")
    void 존재하지_않는_timeId로_주문시_예외가_발생한다() {
        ReservationCreateCommand command = new ReservationCreateCommand(OWNER, FUTURE_DATE, 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(
                ReservationTimeNotFoundException.class,
                () -> userReservationService.createOrder(command)
        );

        verify(reservationTimeRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationRepository, paymentService, paymentOrderRepository);
    }

    @Test
    @DisplayName("결제가 승인되면 예약을 생성하고 주문을 확정 처리한다")
    void 결제_승인시_예약을_생성하고_주문을_확정한다() {
        PaymentConfirmCommand command = new PaymentConfirmCommand("test_pk_1", "order-1", 1000L);
        PaymentOrder order = PaymentOrder.pending("order-1", OWNER, FUTURE_DATE, 1L, 1L, 1000L);
        ReservationResult created = new ReservationResult(
                10L, OWNER, FUTURE_DATE,
                new roomescape.service.dto.ReservationTimeResult(1L, LocalTime.of(10, 0)),
                new roomescape.service.dto.ThemeResult(1L, "무인도 탈출", "설명", "https://example.com/thumb.jpg"),
                0L, ReservationStatus.CONFIRMED);
        given(paymentService.confirm("test_pk_1", "order-1", 1000L))
                .willReturn(new PaymentResult("test_pk_1", "order-1", PaymentStatus.DONE, 1000L));
        given(paymentOrderRepository.getByOrderId("order-1")).willReturn(order);
        given(reservationService.create(any(ReservationCreateCommand.class))).willReturn(created);

        ReservationResult result = userReservationService.confirm(command);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(paymentService, times(1)).confirm("test_pk_1", "order-1", 1000L);
        verify(reservationService, times(1)).create(any(ReservationCreateCommand.class));
        verify(paymentOrderRepository, times(1)).markConfirmed("order-1", "test_pk_1", 10L);
    }

    @Test
    @DisplayName("이름으로 예약 목록을 조회한다")
    void 이름으로_예약_목록을_조회한다() {
        ReservationWithWaitingOrder reservation = new ReservationWithWaitingOrder(
                1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME, ReservationStatus.WAITING, new WaitingOrder(2));
        given(reservationRepository.findByReserverName(OWNER)).willReturn(List.of(reservation));

        List<ReservationResult> results = userReservationService.findByReserverName(OWNER);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).reserverName()).isEqualTo(OWNER);
        assertThat(results.get(0).waitingOrder()).isEqualTo(2L);
        verify(reservationRepository, times(1)).findByReserverName(OWNER);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("본인 예약을 정상적으로 취소한다")
    void 본인_예약을_정상적으로_취소한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        userReservationService.cancel(1L, OWNER);

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationService, times(1)).cancel(1L);
        verifyNoInteractions(reservationTimeRepository);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 취소하면 ReservationNotFoundException이 발생한다")
    void 존재하지_않는_예약_취소시_예외가_발생한다() {
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(
                ReservationNotFoundException.class,
                () -> userReservationService.cancel(1L, OWNER)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("본인이 아닌 예약을 취소하면 UnauthorizedReservationException이 발생한다")
    void 본인이_아닌_예약_취소시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThrows(
                UnauthorizedReservationException.class,
                () -> userReservationService.cancel(1L, OTHER)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("과거 예약을 취소하면 PastReservationException이 발생한다")
    void 과거_예약_취소시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, PAST_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThrows(
                PastReservationException.class,
                () -> userReservationService.cancel(1L, OWNER)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("본인 예약을 정상적으로 변경한다")
    void 본인_예약을_정상적으로_변경한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, ANOTHER_FUTURE_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(ANOTHER_TIME));
        stubThemeLock(Optional.of(VALID_THEME));
        given(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
                OWNER, ANOTHER_FUTURE_DATE, 2L, VALID_THEME.getId(), 1L)).willReturn(false);
        given(reservationWriter.updateAndRequeue(any(Reservation.class))).willAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            return new ReservationWithWaitingOrder(
                    r.getId(), r.getReserverName(), r.getDate(), r.getTime(), r.getTheme(),
                    r.getStatus(), new WaitingOrder(0));
        });

        ReservationResult result = userReservationService.update(command);

        assertThat(result.date()).isEqualTo(ANOTHER_FUTURE_DATE);
        assertThat(result.time().id()).isEqualTo(2L);
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationTimeRepository, times(1)).findById(2L);
        verify(reservationRepository, times(1)).existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
                OWNER, ANOTHER_FUTURE_DATE, 2L, VALID_THEME.getId(), 1L);
        verify(reservationWriter, times(1)).updateAndRequeue(any(Reservation.class));
        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("같은 슬롯으로 변경하면 쓰기 없이 현재 상태를 반환한다")
    void 같은_슬롯_변경은_쓰기없이_조회만_한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.WAITING);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, FUTURE_DATE, VALID_TIME.getId());
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(VALID_TIME.getId())).willReturn(Optional.of(VALID_TIME));
        given(reservationRepository.findWithWaitingOrderById(1L)).willReturn(Optional.of(new ReservationWithWaitingOrder(
                1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME, ReservationStatus.WAITING, new WaitingOrder(3))));

        ReservationResult result = userReservationService.update(command);

        assertThat(result.waitingOrder()).isEqualTo(3L);
        verify(reservationRepository, never()).executeWithThemeLock(any(), any());
        verifyNoInteractions(reservationWriter);
    }

    @Test
    @DisplayName("본인이 아닌 예약을 변경하면 UnauthorizedReservationException이 발생한다")
    void 본인이_아닌_예약_변경시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OTHER, ANOTHER_FUTURE_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThrows(
                UnauthorizedReservationException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 변경하면 ReservationTimeNotFoundException이 발생한다")
    void 존재하지_않는_timeId로_변경시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, ANOTHER_FUTURE_DATE, 99L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(
                ReservationTimeNotFoundException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationTimeRepository, times(1)).findById(99L);
        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("변경 시점이 과거이면 PastReservationException이 발생한다")
    void 변경_시점이_과거이면_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, PAST_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(ANOTHER_TIME));

        assertThrows(
                PastReservationException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationTimeRepository, times(1)).findById(2L);
        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("기존 예약이 과거이면 PastReservationException이 발생한다")
    void 기존_예약이_과거이면_변경할_수_없다() {
        Reservation reservation = new Reservation(1L, OWNER, PAST_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, ANOTHER_FUTURE_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThrows(
                PastReservationException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verifyNoInteractions(reservationService, reservationTimeRepository);
    }

    @Test
    @DisplayName("본인이 이미 예약 또는 대기중인 시간으로 변경하면 ReservationConflictException이 발생한다")
    void 변경_시간_충돌시_예외가_발생한다() {
        Reservation reservation = new Reservation(1L, OWNER, FUTURE_DATE, VALID_TIME, VALID_THEME,
                ReservationStatus.CONFIRMED);
        ReservationUpdateCommand command = new ReservationUpdateCommand(1L, OWNER, ANOTHER_FUTURE_DATE, 2L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(ANOTHER_TIME));
        stubThemeLock(Optional.of(VALID_THEME));
        given(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
                OWNER, ANOTHER_FUTURE_DATE, 2L, VALID_THEME.getId(), 1L)).willReturn(true);

        assertThrows(
                ReservationConflictException.class,
                () -> userReservationService.update(command)
        );

        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationTimeRepository, times(1)).findById(2L);
        verify(reservationRepository, times(1)).existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
                OWNER, ANOTHER_FUTURE_DATE, 2L, VALID_THEME.getId(), 1L);
        verifyNoInteractions(reservationService);
    }
}
