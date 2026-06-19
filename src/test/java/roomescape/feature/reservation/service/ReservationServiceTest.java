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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import roomescape.feature.payment.PaymentApprover;
import roomescape.feature.reservation.cancel.SlotReleasedEvent;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.command.ReservationUpdateCommand;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationEditableStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.Slot;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.mapper.ThemeMapper;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;
import roomescape.feature.time.mapper.TimeMapper;
import roomescape.feature.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

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

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        ReservationMapper mapper = new ReservationMapper(new TimeMapper(), new ThemeMapper());
        reservationService = new ReservationManagementService(
            reservationRepository, timeRepository, themeRepository, paymentApprover, mapper, eventPublisher);
    }

    private Time timeWithId(Long id) {
        return Time.reconstruct(id, LocalTime.of(10, 0), EntityStatus.ACTIVE);
    }

    private Theme themeWithId(Long id) {
        return Theme.reconstruct(id, "테마 이름", "테마 설명", "https://example.com/theme.png", EntityStatus.ACTIVE);
    }

    @Nested
    class 이름으로_예약_조회 {

        @Test
        void 이름에_해당하는_예약이_없으면_빈_목록을_반환한다() {
            when(reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("예약자"))).thenReturn(List.of());

            assertThat(reservationService.getReservationsByName(new ReserverName("예약자"))).isEmpty();
        }

        @Test
        void 대기_예약은_순번을_포함하여_WAITING_상태로_반환한다() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation waiting = Reservation.reconstruct(
                2L, new ReserverName("예약자"), date, time, theme, ReservationStatus.WAITING);
            when(reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("예약자")))
                .thenReturn(List.of(waiting));
            when(reservationRepository.countByIdLessThanEqualAndSlot(2L, waiting.getSlot().toSlotKey()))
                .thenReturn(2);

            // when
            List<ReservationResponseDto> result = reservationService.getReservationsByName(new ReserverName("예약자"));

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.WAITING);
            assertThat(result.getFirst().waitingNumber()).isEqualTo(2);
        }

        @Test
        void 대기_중인_예약이_아니라면_순번을_계산하지_않는다() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation active = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("예약자")))
                .thenReturn(List.of(active));

            // when
            reservationService.getReservationsByName(new ReserverName("예약자"));

            // then
            verify(reservationRepository, never())
                .countByIdLessThanEqualAndSlot(any(), any());
        }

        @Test
        void 지난_날짜의_예약은_LOCKED_상태로_반환한다() {
            // given
            LocalDate pastDate = LocalDate.now().minusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation past = Reservation.reconstruct(
                1L, new ReserverName("예약자"), pastDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("예약자")))
                .thenReturn(List.of(past));

            // when
            List<ReservationResponseDto> result = reservationService.getReservationsByName(new ReserverName("예약자"));

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.LOCKED);
        }

        @Test
        void 삭제된_시간이나_테마가_있는_예약은_EDIT_RECOMMENDED_상태로_반환한다() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Time deletedTime = Time.reconstruct(1L, LocalTime.of(10, 0), EntityStatus.DELETED);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, deletedTime, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("예약자")))
                .thenReturn(List.of(reservation));

            // when
            List<ReservationResponseDto> result = reservationService.getReservationsByName(new ReserverName("예약자"));

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.EDIT_RECOMMENDED);
        }
    }

    @Nested
    class 예약_생성 {

        @Test
        void 예약을_생성한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.now().plusYears(1);
            ReservationCreateCommand command = new ReservationCreateCommand(
                new ReserverName("예약자"), date, 1L, 1L);
            Reservation saved = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, time, theme, ReservationStatus.ACTIVE);
            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsActiveOrWaitingReservation(new Slot(date, time, theme).toSlotKey()))
                .thenReturn(false);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

            // when
            ReservationCreateResponseDto result = reservationService.saveReservation(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("예약자");
            assertThat(result.date()).isEqualTo(date);
            assertThat(result.timeId()).isEqualTo(1L);
            assertThat(result.themeId()).isEqualTo(1L);

            verify(reservationRepository).save(any(Reservation.class));
        }

        @Test
        void 존재하지_않는_timeId로_예약_생성_시_파라미터_에러가_발생한다() {
            // given
            Theme theme = themeWithId(1L);
            ReservationCreateCommand command = new ReservationCreateCommand(
                new ReserverName("예약자"), LocalDate.now().plusDays(1), 999L, 1L);
            when(timeRepository.findTimeByIdAndNotDeleted(999L)).thenReturn(Optional.empty());
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));

            // when & then
            assertThatThrownBy(() -> reservationService.saveReservation(command))
                .isInstanceOfSatisfying(GeneralParametersException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo("조회할 자원이 존재하지 않습니다.");
                    assertThat(ex.getParameterErrors())
                        .extracting(ParameterErrorResponseDto::parameter)
                        .containsExactly("timeId");
                });

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        void timeId와_themeId가_모두_존재하지_않으면_파라미터_에러를_모두_포함한다() {
            // given
            ReservationCreateCommand command = new ReservationCreateCommand(
                new ReserverName("예약자"), LocalDate.now().plusDays(1), 999L, 999L);
            when(timeRepository.findTimeByIdAndNotDeleted(999L)).thenReturn(Optional.empty());
            when(themeRepository.findThemeByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.saveReservation(command))
                .isInstanceOfSatisfying(GeneralParametersException.class, ex -> {
                    assertThat(ex.getParameterErrors())
                        .extracting(ParameterErrorResponseDto::parameter)
                        .containsExactly("timeId", "themeId");
                });

            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        void 이미_예약된_날짜_시간_테마에_예약하면_예외가_발생한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.now().plusDays(1);
            ReservationCreateCommand command = new ReservationCreateCommand(
                new ReserverName("예약자"), date, 1L, 1L);
            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsActiveOrWaitingReservation(new Slot(date, time, theme).toSlotKey()))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> reservationService.saveReservation(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 예약된 날짜, 시간, 테마입니다.");

            verify(reservationRepository, never()).save(any(Reservation.class));
        }
    }

    @Nested
    class 예약_수정 {

        @Test
        void 예약을_수정한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time existingTime = timeWithId(1L);
            Theme existingTheme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                    1L, new ReserverName("예약자"), futureDate, existingTime, existingTheme, ReservationStatus.ACTIVE);

            LocalDate newDate = futureDate.plusDays(1);
            Time newTime = Time.reconstruct(2L, LocalTime.of(11, 0), EntityStatus.ACTIVE);
            Theme newTheme = Theme.reconstruct(2L, "새 테마", "새 설명", "https://example.com/new.png", EntityStatus.ACTIVE);
            Reservation updated = Reservation.reconstruct(
                1L, new ReserverName("예약자"), newDate, newTime, newTheme, ReservationStatus.ACTIVE);

            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), newDate, 2L, 2L);

            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(existing));
            when(timeRepository.findTimeByIdAndNotDeleted(2L)).thenReturn(Optional.of(newTime));
            when(themeRepository.findThemeByIdAndNotDeleted(2L)).thenReturn(Optional.of(newTheme));
            when(reservationRepository.existsActiveOrWaitingReservation(updated.getSlot().toSlotKey())).thenReturn(false);
            when(reservationRepository.update(any(Reservation.class))).thenReturn(updated);

            // when
            ReservationCreateResponseDto result = reservationService.updateReservation(1L, command);

            // then
            assertThat(result.date()).isEqualTo(newDate);
            assertThat(result.timeId()).isEqualTo(2L);
            assertThat(result.themeId()).isEqualTo(2L);

            verify(reservationRepository).update(any(Reservation.class));
        }

        @Test
        void 수정하면_원래_슬롯의_대기_확정을_위한_이벤트를_발행한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time existingTime = timeWithId(1L);
            Theme existingTheme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                    1L, new ReserverName("예약자"), futureDate, existingTime, existingTheme, ReservationStatus.ACTIVE);

            LocalDate newDate = futureDate.plusDays(1);
            Time newTime = Time.reconstruct(2L, LocalTime.of(11, 0), EntityStatus.ACTIVE);
            Theme newTheme = Theme.reconstruct(2L, "새 테마", "새 설명", "https://example.com/new.png", EntityStatus.ACTIVE);
            Reservation updated = Reservation.reconstruct(
                1L, new ReserverName("예약자"), newDate, newTime, newTheme, ReservationStatus.ACTIVE);

            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), newDate, 2L, 2L);

            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(existing));
            when(timeRepository.findTimeByIdAndNotDeleted(2L)).thenReturn(Optional.of(newTime));
            when(themeRepository.findThemeByIdAndNotDeleted(2L)).thenReturn(Optional.of(newTheme));
            when(reservationRepository.existsActiveOrWaitingReservation(updated.getSlot().toSlotKey())).thenReturn(false);
            when(reservationRepository.update(any(Reservation.class))).thenReturn(updated);

            // when
            reservationService.updateReservation(1L, command);

            // then: 비워진 원래 슬롯(timeId=1, themeId=1, futureDate) 기준으로 이벤트 발행
            ArgumentCaptor<SlotReleasedEvent> captor =
                ArgumentCaptor.forClass(SlotReleasedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().slotKey().timeId()).isEqualTo(1L);
            assertThat(captor.getValue().slotKey().themeId()).isEqualTo(1L);
            assertThat(captor.getValue().slotKey().date()).isEqualTo(futureDate);
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), LocalDate.now().plusDays(1), 1L, 1L);
            when(reservationRepository.findReservationByIdAndNotDeleted(999L))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(999L, command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");

            verify(timeRepository, never()).findTimeByIdAndNotDeleted(any());
            verify(reservationRepository, never()).update(any(Reservation.class));
        }

        @Test
        void 예약자_이름이_다르면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("다른사람"), futureDate, 1L, 1L);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(existing));
            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 변경할 권한이 없습니다.");

            verify(reservationRepository, never()).update(any(Reservation.class));
        }

        @Test
        void 활성_예약이_아니면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation canceled = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.CANCELED);
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), futureDate, 1L, 1L);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(canceled));
            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("활성된 예약이 아닙니다.");

            verify(reservationRepository, never()).update(any(Reservation.class));
        }

        @Test
        void 지난_날짜의_예약이면_예외가_발생한다() {
            // given
            LocalDate pastDate = LocalDate.now().minusDays(1);
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation past = Reservation.reconstruct(
                1L, new ReserverName("예약자"), pastDate, time, theme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), futureDate, 1L, 1L);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(past));
            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 생성할 수 없습니다");

            verify(reservationRepository, never()).update(any(Reservation.class));
        }

        @Test
        void 새_날짜와_시간이_과거이면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            LocalDate pastDate = LocalDate.now().minusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), pastDate, 1L, 1L);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(existing));
            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 생성할 수 없습니다");

            verify(reservationRepository, never()).update(any(Reservation.class));
        }

        @Test
        void 변경할_timeId가_존재하지_않으면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time existingTime = timeWithId(1L);
            Theme existingTheme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, existingTime, existingTheme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), futureDate, 999L, 1L);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(existing));
            when(timeRepository.findTimeByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("수정할 자원이 존재하지 않습니다.");

            verify(reservationRepository, never()).update(any(Reservation.class));
        }

        @Test
        void 변경할_themeId가_존재하지_않으면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time existingTime = timeWithId(1L);
            Theme existingTheme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, existingTime, existingTheme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), futureDate, 1L, 999L);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(existing));
            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(existingTime));
            when(themeRepository.findThemeByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("수정할 자원이 존재하지 않습니다.");

            verify(reservationRepository, never()).update(any(Reservation.class));
        }

        @Test
        void 다른_예약과_날짜_시간_테마가_중복되면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate.plusDays(1), time, theme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                new ReserverName("예약자"), futureDate, 1L, 1L);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(existing));
            when(timeRepository.findTimeByIdAndNotDeleted(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndNotDeleted(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsActiveOrWaitingReservation(
                new Slot(futureDate, time, theme).toSlotKey())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 예약된 날짜, 시간, 테마입니다.");

            verify(reservationRepository, never()).update(any(Reservation.class));
        }
    }

    @Nested
    class 예약_취소 {

        @Test
        void 예약을_취소한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(reservation));

            // when
            ReservationCancelResponseDto result = reservationService.cancelReservation(1L, new ReserverName("예약자"));

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("예약자");

            verify(reservationRepository).changeStatus(1L, 0L, ReservationStatus.ACTIVE, ReservationStatus.CANCELED);
        }

        @Test
        void 취소하면_대기_확정을_위한_이벤트를_발행한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(reservation));

            // when
            reservationService.cancelReservation(1L, new ReserverName("예약자"));

            // then
            ArgumentCaptor<SlotReleasedEvent> captor = ArgumentCaptor.forClass(
                    SlotReleasedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().slotKey().timeId()).isEqualTo(1L);
            assertThat(captor.getValue().slotKey().themeId()).isEqualTo(1L);
            assertThat(captor.getValue().slotKey().date()).isEqualTo(futureDate);
        }

        @Test
        void 취소_직전_상태가_바뀌어_갱신되지_않으면_낙관적_락_예외가_발생하고_이벤트를_발행하지_않는다() {
            // given
            LocalDate futureDate = LocalDate.now().plusYears(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(reservation));
            doThrow(new OptimisticLockingFailureException("동시 변경 충돌"))
                .when(reservationRepository)
                .changeStatus(1L, 0L, ReservationStatus.ACTIVE, ReservationStatus.CANCELED);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, new ReserverName("예약자")))
                .isInstanceOf(OptimisticLockingFailureException.class);

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            when(reservationRepository.findReservationByIdAndNotDeleted(999L))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(999L, new ReserverName("예약자")))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");

            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
            verify(eventPublisher, never()).publishEvent(any(SlotReleasedEvent.class));
        }

        @Test
        void 예약자_이름이_다르면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(reservation));

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, new ReserverName("다른사람")))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 취소할 권한이 없습니다.");

            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
            verify(eventPublisher, never()).publishEvent(any(SlotReleasedEvent.class));
        }

        @Test
        void 활성_예약이_아니면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation canceled = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.CANCELED);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(canceled));

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, new ReserverName("예약자")))
                .isInstanceOf(GeneralException.class)
                .hasMessage("활성된 예약이 아닙니다.");

            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        void 지난_날짜의_예약이면_예외가_발생한다() {
            // given
            LocalDate pastDate = LocalDate.now().minusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation past = Reservation.reconstruct(
                1L, new ReserverName("예약자"), pastDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(past));

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, new ReserverName("예약자")))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 취소할 수 없습니다.");

            verify(reservationRepository, never()).changeStatus(any(), anyLong(), any(), any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }
}
