package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import roomescape.common.domain.ReservationSlot;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFactory;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private final Clock fixedClock = Clock.fixed(
            LocalDate.now().atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeService reservationTimeService;
    @Mock
    private ThemeService themeService;
    @Mock
    private ReservationFactory reservationFactory;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private Clock clock;
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationTime time1;
    private ReservationTime time2;
    private ReservationTime timeWithin12Hours;
    private Theme theme;
    private Reservation futureReservation;
    private Reservation pastReservation;
    private Reservation within12HoursReservation;

    @BeforeEach
    void setUp() {
        time1 = ReservationTime.restore(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        time2 = ReservationTime.restore(2L, LocalTime.of(16, 0), LocalTime.of(17, 0));
        timeWithin12Hours = ReservationTime.restore(3L, LocalTime.of(20, 0), LocalTime.of(21, 0));
        theme = Theme.restore(1L, "테마A", "설명A", "https://a.com", 20000);
        futureReservation = Reservation.restore(1L, "user1",
                new ReservationSlot(LocalDate.of(2099, 12, 1), time1, theme), PaymentStatus.CONFIRMED);
        pastReservation = Reservation.restore(1L, "user1",
                new ReservationSlot(LocalDate.now().minusDays(1), time1, theme), PaymentStatus.CONFIRMED);
        within12HoursReservation = Reservation.restore(1L, "user1",
                new ReservationSlot(LocalDate.now(), timeWithin12Hours, theme), PaymentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("존재하지 않는 timeId로 예약 생성 시 예외 발생")
    void 존재하지_않는_timeId_예외() {
        when(reservationTimeService.getById(Long.MAX_VALUE)).thenThrow(new BusinessException(ErrorCode.TIME_NOT_FOUND));

        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now().plusDays(1), Long.MAX_VALUE, 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.TIME_NOT_FOUND))
                .hasMessage(ErrorCode.TIME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 themeId로 예약 생성 시 예외 발생")
    void 존재하지_않는_themeId_예외() {
        when(reservationTimeService.getById(1L)).thenReturn(time1);
        when(themeService.getById(Long.MAX_VALUE)).thenThrow(new BusinessException(ErrorCode.THEME_NOT_FOUND));

        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now().plusDays(1), 1L, Long.MAX_VALUE)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_NOT_FOUND))
                .hasMessage(ErrorCode.THEME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약 시작 12시간 이내에는 취소할 수 없다")
    void 정책_12시간_이내_예약_취소_불가() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(within12HoursReservation));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_RESERVATION_CANCEL))
                .hasMessage(ErrorCode.PAST_RESERVATION_CANCEL.getMessage());
    }

    @Test
    @DisplayName("예약 시작 12시간 이내에는 수정할 수 없다")
    void 정책_12시간_이내_예약_수정_불가() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(within12HoursReservation));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        assertThatThrownBy(() -> reservationService.updateReservation(
                1L, new ReservationUpdateRequest(LocalDate.of(2099, 12, 2), 2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_RESERVATION_UPDATE))
                .hasMessage(ErrorCode.PAST_RESERVATION_UPDATE.getMessage());
    }

    @Test
    @DisplayName("이미 지난 예약은 취소할 수 없다")
    void 과거_예약_취소_불가() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pastReservation));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_RESERVATION_CANCEL))
                .hasMessage(ErrorCode.PAST_RESERVATION_CANCEL.getMessage());
    }

    @Test
    @DisplayName("이미 지난 예약은 수정할 수 없다")
    void 과거_예약_수정_불가() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pastReservation));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        assertThatThrownBy(() -> reservationService.updateReservation(
                1L, new ReservationUpdateRequest(LocalDate.of(2099, 12, 2), 2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_RESERVATION_UPDATE))
                .hasMessage(ErrorCode.PAST_RESERVATION_UPDATE.getMessage());
    }

    @Test
    @DisplayName("변경하려는 날짜·시간이 과거면 수정 불가")
    void 새시간_과거면_수정_불가() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(futureReservation));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(reservationRepository.isBookedByOther(any(), any())).thenReturn(false);
        when(reservationTimeService.getById(2L)).thenReturn(time2);

        assertThatThrownBy(() -> reservationService.updateReservation(
                1L, new ReservationUpdateRequest(LocalDate.now().minusDays(1), 2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.PAST_TIME_RESERVATION))
                .hasMessage(ErrorCode.PAST_TIME_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("오늘 날짜에 이미 지난 시각으로 예약 불가")
    void 오늘_날짜_지난_시각_예약_불가() {
        when(reservationTimeService.getById(1L)).thenReturn(time1);
        when(themeService.getById(1L)).thenReturn(theme);
        when(reservationRepository.isBooked(any())).thenReturn(false);
        when(reservationFactory.create(any(), any())).thenThrow(
                new BusinessException(ErrorCode.PAST_TIME_CREATE));

        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest("현미밥", LocalDate.now(), 1L, 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.PAST_TIME_CREATE))
                .hasMessage(ErrorCode.PAST_TIME_CREATE.getMessage());
    }

    @Test
    @DisplayName("변경하려는 시간이 이미 예약된 경우 수정 불가")
    void 중복_예약_수정_불가() {
        Reservation reservation = Reservation.restore(2L, "user2",
                new ReservationSlot(LocalDate.of(2099, 12, 1), time2, theme), PaymentStatus.CONFIRMED);
        when(reservationRepository.findById(2L)).thenReturn(Optional.of(reservation));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(reservationRepository.isBookedByOther(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.updateReservation(2L,
                new ReservationUpdateRequest(LocalDate.of(2099, 12, 1), 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(
                        ErrorCode.DUPLICATE_RESERVATION))
                .hasMessage(ErrorCode.DUPLICATE_RESERVATION.getMessage());
    }
}
