package roomescape.feature.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import roomescape.feature.payment.PaymentApprover;
import roomescape.feature.payment.PaymentProperties;
import roomescape.feature.reservation.cancel.SlotReleasedEvent;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.Slot;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.mapper.ThemeMapper;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.mapper.TimeMapper;
import roomescape.feature.time.repository.TimeRepository;
import roomescape.global.domain.EntityStatus;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TimeRepository timeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private PaymentApprover paymentApprover;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        ReservationMapper mapper = new ReservationMapper(new TimeMapper(), new ThemeMapper());
        waitingService = new ReservationManagementService(
            reservationRepository, timeRepository, themeRepository, paymentApprover,
            new PaymentProperties("test_ck", 1_000L), mapper, eventPublisher);
    }

    private Time timeWithId(Long id) {
        return Time.reconstruct(id, LocalTime.of(10, 0), EntityStatus.ACTIVE);
    }

    private Theme themeWithId(Long id) {
        return Theme.reconstruct(id, "테마 이름", "테마 설명", "https://example.com/theme.png", EntityStatus.ACTIVE);
    }

    @Nested
    class 예약_대기_생성 {

        @Test
        void 예약_대기를_생성한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.now().plusYears(1);
            ReservationCreateCommand command = new ReservationCreateCommand(
                new ReserverName("예약자"), date, 1L, 1L);
            Reservation saved = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, time, theme, ReservationStatus.WAITING);

            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsReservationAndStatus(any(Reservation.class), any()))
                .thenReturn(false);
            when(reservationRepository.existsActiveReservation(new Slot(date, time, theme).toSlotKey()))
                .thenReturn(true);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

            // when
            ReservationCreateResponseDto result = waitingService.saveWaitingReservation(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("예약자");

            verify(reservationRepository).save(any(Reservation.class));
        }

        @Test
        void 해당_날짜_시간_테마에_예약이_없으면_예외가_발생한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.now().plusYears(1);
            ReservationCreateCommand command = new ReservationCreateCommand(
                new ReserverName("예약자"), date, 1L, 1L);

            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsReservationAndStatus(any(Reservation.class), any()))
                .thenReturn(false);
            when(reservationRepository.existsActiveReservation(new Slot(date, time, theme).toSlotKey()))
                .thenReturn(false);

            // when & then
            assertThatThrownBy(() -> waitingService.saveWaitingReservation(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("아직 예약되지 않은 날짜, 시간, 테마입니다.");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        void 같은_이름_날짜_시간_테마로_이미_대기_중이면_예외가_발생한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.now().plusYears(1);
            ReservationCreateCommand command = new ReservationCreateCommand(
                new ReserverName("예약자"), date, 1L, 1L);

            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsReservationAndStatus(
                any(Reservation.class), any(ReservationStatus.class)))
                .thenAnswer(invocation -> {
                    ReservationStatus status = invocation.getArgument(1);
                    return status == ReservationStatus.WAITING;
                });

            // when & then
            assertThatThrownBy(() -> waitingService.saveWaitingReservation(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 대기 중인 이름, 날짜, 시간, 테마입니다.");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        void 존재하지_않는_timeId로_대기_생성_시_파라미터_에러가_발생한다() {
            // given
            ReservationCreateCommand command = new ReservationCreateCommand(
                new ReserverName("예약자"), LocalDate.now().plusDays(1), 999L, 999L);

            when(timeRepository.findTimeByIdAndNotDeleted(999L)).thenReturn(Optional.empty());
            when(themeRepository.findThemeByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> waitingService.saveWaitingReservation(command))
                .isInstanceOfSatisfying(GeneralParametersException.class, ex -> {
                    assertThat(ex.getParameterErrors())
                        .extracting(ParameterErrorResponseDto::parameter)
                        .containsExactly("timeId", "themeId");
                });

            verify(reservationRepository, never()).save(any(Reservation.class));
        }
    }

    @Nested
    class 예약_대기_취소 {

        @Test
        void 예약_대기를_취소한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation waiting = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.WAITING);

            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(waiting));

            // when
            ReservationCancelResponseDto result = waitingService.cancelWaitingReservation(1L, new ReserverName("예약자"));

            // then
            assertThat(result.id()).isEqualTo(1L);

            verify(reservationRepository).changeStatus(1L, 0L, ReservationStatus.WAITING, ReservationStatus.CANCELED);
            verify(eventPublisher, never()).publishEvent(any(SlotReleasedEvent.class));
        }

        @Test
        void 취소_직전_상태가_바뀌어_갱신되지_않으면_낙관적_락_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation waiting = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.WAITING);

            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(waiting));
            doThrow(new OptimisticLockingFailureException("동시 변경 충돌"))
                .when(reservationRepository)
                .changeStatus(1L, 0L, ReservationStatus.WAITING, ReservationStatus.CANCELED);

            // when & then
            assertThatThrownBy(() -> waitingService.cancelWaitingReservation(1L, new ReserverName("예약자")))
                .isInstanceOf(OptimisticLockingFailureException.class);
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            when(reservationRepository.findReservationByIdAndNotDeleted(999L))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> waitingService.cancelWaitingReservation(999L, new ReserverName("예약자")))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");

            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
        }

        @Test
        void 예약자_이름이_다르면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation waiting = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.WAITING);

            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(waiting));

            // when & then
            assertThatThrownBy(() -> waitingService.cancelWaitingReservation(1L, new ReserverName("다른사람")))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 취소할 권한이 없습니다.");

            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
        }

        @Test
        void 대기_예약이_아니면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation active = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.ACTIVE);

            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(active));

            // when & then
            assertThatThrownBy(() -> waitingService.cancelWaitingReservation(1L, new ReserverName("예약자")))
                .isInstanceOf(GeneralException.class)
                .hasMessage("대기중인 예약이 아닙니다.");

            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
        }

        @Test
        void 지난_날짜의_대기_예약이면_예외가_발생한다() {
            // given
            LocalDate pastDate = LocalDate.now().minusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation pastWaiting = Reservation.reconstruct(
                1L, new ReserverName("예약자"), pastDate, time, theme, ReservationStatus.WAITING);

            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(pastWaiting));

            // when & then
            assertThatThrownBy(() -> waitingService.cancelWaitingReservation(1L, new ReserverName("예약자")))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 취소할 수 없습니다.");

            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
        }
    }
}
