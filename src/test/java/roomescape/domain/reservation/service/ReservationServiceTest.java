package roomescape.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.reservation.dto.command.ReservationCreateCommand;
import roomescape.domain.reservation.dto.command.ReservationUpdateCommand;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationEditableStatus;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.mapper.ReservationMapper;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationWithWaitingNumber;
import roomescape.domain.reservation.vo.ReservationSchedule;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.mapper.TimeMapper;
import roomescape.domain.time.repository.TimeRepository;
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

    private ReservationService reservationService;

    private final Clock fixedClock = Clock.fixed(
        Instant.parse("2026-05-08T00:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    @BeforeEach
    void setUp() {
        ReservationMapper mapper = new ReservationMapper(new TimeMapper(), new ThemeMapper());
        reservationService = new ReservationService(
            reservationRepository, timeRepository, themeRepository, mapper, fixedClock);
    }

    private Time timeWithId(Long id) {
        return timeWithId(id, LocalTime.of(10, 0));
    }

    private Time timeWithId(Long id, LocalTime startAt) {
        return Time.reconstruct(id, startAt, null);
    }

    private Theme themeWithId(Long id) {
        return Theme.reconstruct(id, "테마 이름", "테마 설명", "https://example.com/theme.png", null);
    }

    @Nested
    class 예약_목록_조회 {

        @Test
        void 예약이_없으면_빈_목록을_반환한다() {
            when(reservationRepository.findReservationsByNotDeletedWithWaitingNumber()).thenReturn(List.of());

            assertThat(reservationService.getReservations()).isEmpty();
        }

        @Test
        void 미래_활성_예약은_EDITABLE_상태로_반환한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationsByNotDeletedWithWaitingNumber())
                .thenReturn(List.of(new ReservationWithWaitingNumber(reservation, null)));

            // when
            List<ReservationResponseDto> result = reservationService.getReservations();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.EDITABLE);
        }

        @Test
        void 오늘_날짜라도_지난_시간의_예약은_LOCKED_상태로_반환한다() {
            // given
            LocalDate today = LocalDate.now(fixedClock);
            Time pastTime = timeWithId(1L, LocalTime.of(8, 0));
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, "예약자", today, pastTime, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationsByNotDeletedWithWaitingNumber())
                .thenReturn(List.of(new ReservationWithWaitingNumber(reservation, null)));

            // when
            List<ReservationResponseDto> result = reservationService.getReservations();

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.LOCKED);
        }

        @Test
        void 오늘_날짜라도_지난_시간의_대기_예약은_WAITING_LOCKED_상태로_반환한다() {
            // given
            LocalDate today = LocalDate.now(fixedClock);
            Time pastTime = timeWithId(1L, LocalTime.of(8, 0));
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, "예약자", today, pastTime, theme, ReservationStatus.WAITING);
            when(reservationRepository.findReservationsByNotDeletedWithWaitingNumber())
                .thenReturn(List.of(new ReservationWithWaitingNumber(reservation, 1)));

            // when
            List<ReservationResponseDto> result = reservationService.getReservations();

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.WAITING_LOCKED);
        }

        @Test
        void 취소된_예약은_CANCELED_상태로_반환한다() {
            // given
            LocalDate date = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation canceled = Reservation.reconstruct(
                1L, "예약자", date, time, theme, ReservationStatus.CANCELED);
            when(reservationRepository.findReservationsByNotDeletedWithWaitingNumber())
                .thenReturn(List.of(new ReservationWithWaitingNumber(canceled, null)));

            // when
            List<ReservationResponseDto> result = reservationService.getReservations();

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.CANCELED);
        }
    }

    @Nested
    class 이름으로_예약_조회 {

        @Test
        void 이름에_해당하는_예약이_없으면_빈_목록을_반환한다() {
            when(reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("예약자"))
                .thenReturn(List.of());

            assertThat(reservationService.getReservationsByName("예약자")).isEmpty();
        }

        @Test
        void 대기_예약은_순번을_포함하여_WAITING_상태로_반환한다() {
            // given
            LocalDate date = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation waiting = Reservation.reconstruct(
                2L, "예약자", date, time, theme, ReservationStatus.WAITING);
            when(reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("예약자"))
                .thenReturn(List.of(new ReservationWithWaitingNumber(waiting, 2)));

            // when
            List<ReservationResponseDto> result = reservationService.getReservationsByName("예약자");

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.WAITING);

            assertThat(result.getFirst().waitingNumber()).isEqualTo(2);
        }

        @Test
        void 지난_날짜의_예약은_LOCKED_상태로_반환한다() {
            // given
            LocalDate pastDate = LocalDate.now(fixedClock).minusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation past = Reservation.reconstruct(
                1L, "예약자", pastDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("예약자"))
                .thenReturn(List.of(new ReservationWithWaitingNumber(past, null)));

            // when
            List<ReservationResponseDto> result = reservationService.getReservationsByName("예약자");

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.LOCKED);
        }

        @Test
        void 삭제된_시간이나_테마가_있는_예약은_EDIT_RECOMMENDED_상태로_반환한다() {
            // given
            LocalDate date = LocalDate.now(fixedClock).plusDays(1);
            Time deletedTime = Time.reconstruct(1L, LocalTime.of(10, 0), LocalDateTime.now());
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, "예약자", date, deletedTime, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("예약자"))
                .thenReturn(List.of(new ReservationWithWaitingNumber(reservation, null)));

            // when
            List<ReservationResponseDto> result = reservationService.getReservationsByName("예약자");

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.EDIT_RECOMMENDED);
        }
    }

    @Nested
    class 내_예약_조회 {

        @Test
        void 메서드_이름_쿼리로_본인의_예약_목록을_조회한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationByName("예약자"))
                .thenReturn(List.of(reservation));

            // when
            List<ReservationResponseDto> result = reservationService.getMineReservations("예약자");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("예약자");
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.EDITABLE);
            assertThat(result.getFirst().waitingNumber()).isNull();
        }
    }

    @Nested
    class 예약_생성 {

        @Test
        void 예약을_생성한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.of(2026, 5, 20);
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", date, 1L, 1L);
            Reservation saved = Reservation.reconstruct(
                1L, "예약자", date, time, theme, ReservationStatus.ACTIVE);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

            // when
            ReservationCreateResponseDto result = reservationService.saveReservation(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("예약자");
            assertThat(result.date()).isEqualTo(date);
            assertThat(result.timeId()).isEqualTo(1L);
            assertThat(result.themeId()).isEqualTo(1L);
        }

        @Test
        void 존재하지_않는_timeId로_예약_생성_시_파라미터_에러가_발생한다() {
            // given
            Theme theme = themeWithId(1L);
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", LocalDate.of(2026, 5, 20), 999L, 1L);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(theme));

            // when & then
            assertThatThrownBy(() -> reservationService.saveReservation(command))
                .isInstanceOfSatisfying(GeneralParametersException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo("조회할 자원이 존재하지 않습니다.");
                    assertThat(ex.getParameterErrors())
                        .extracting(ParameterErrorResponseDto::parameter)
                        .containsExactly("timeId");
                });
        }

        @Test
        void timeId와_themeId가_모두_존재하지_않으면_파라미터_에러를_모두_포함한다() {
            // given
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", LocalDate.of(2026, 5, 20), 999L, 999L);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.saveReservation(command))
                .isInstanceOfSatisfying(GeneralParametersException.class, ex -> {
                    assertThat(ex.getParameterErrors())
                        .extracting(ParameterErrorResponseDto::parameter)
                        .containsExactly("timeId", "themeId");
                });
        }

        @Test
        void 이미_예약된_날짜_시간_테마에_예약하면_예외가_발생한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.of(2026, 5, 20);
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", date, 1L, 1L);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.save(any(Reservation.class))).thenThrow(new DuplicateKeyException("duplicate"));

            // when & then
            assertThatThrownBy(() -> reservationService.saveReservation(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 예약된 날짜, 시간, 테마입니다.");
        }

        @Test
        void 오늘_날짜라도_지난_시간이면_예약을_생성할_수_없다() {
            // given
            Time pastTime = timeWithId(1L, LocalTime.of(8, 0));
            Theme theme = themeWithId(1L);
            LocalDate today = LocalDate.now(fixedClock);
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", today, 1L, 1L);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(pastTime));
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(theme));

            // when & then
            assertThatThrownBy(() -> reservationService.saveReservation(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 생성할 수 없습니다.");
        }
    }

    @Nested
    class 예약_대기_생성 {

        @Test
        void 예약_대기를_생성한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.of(2026, 5, 20);
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", date, 1L, 1L);
            Reservation saved = Reservation.reconstruct(
                1L, "예약자", date, time, theme, ReservationStatus.WAITING);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsReservationAndStatus(any(Reservation.class), any()))
                .thenReturn(false);
            when(reservationRepository.lockActiveReservationBySchedule(new ReservationSchedule(date, 1L, 1L)))
                .thenReturn(Optional.of(2L));
            when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

            // when
            ReservationCreateResponseDto result = reservationService.saveWaitingReservation(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("예약자");
        }

        @Test
        void 같은_이름_날짜_시간_테마로_이미_대기_중이면_예외가_발생한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.of(2026, 5, 20);
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", date, 1L, 1L);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsReservationAndStatus(
                any(Reservation.class), any(ReservationStatus.class)))
                .thenReturn(false);
            when(reservationRepository.lockActiveReservationBySchedule(new ReservationSchedule(date, 1L, 1L)))
                .thenReturn(Optional.of(2L));
            when(reservationRepository.save(any(Reservation.class))).thenThrow(new DuplicateKeyException("duplicate"));

            // when & then
            assertThatThrownBy(() -> reservationService.saveWaitingReservation(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 대기 중인 이름, 날짜, 시간, 테마입니다.");
        }

        @Test
        void 활성_예약이_없는_날짜_시간_테마에는_대기_예약을_생성할_수_없다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.of(2026, 5, 20);
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", date, 1L, 1L);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsReservationAndStatus(
                any(Reservation.class), any(ReservationStatus.class)))
                .thenReturn(false);
            when(reservationRepository.lockActiveReservationBySchedule(new ReservationSchedule(date, 1L, 1L)))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.saveWaitingReservation(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약 가능한 시간은 대기할 수 없습니다.");
        }

        @Test
        void 같은_이름_날짜_시간_테마로_이미_활성_예약이_있으면_예외가_발생한다() {
            // given
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            LocalDate date = LocalDate.of(2026, 5, 20);
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", date, 1L, 1L);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsReservationAndStatus(
                any(Reservation.class), any(ReservationStatus.class)))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> reservationService.saveWaitingReservation(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 동일한 일정의 예약을 보유하고 있습니다.");
        }

        @Test
        void 존재하지_않는_timeId로_대기_생성_시_파라미터_에러가_발생한다() {
            // given
            ReservationCreateCommand command = new ReservationCreateCommand(
                "예약자", LocalDate.of(2026, 5, 20), 999L, 999L);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.saveWaitingReservation(command))
                .isInstanceOfSatisfying(GeneralParametersException.class, ex -> {
                    assertThat(ex.getParameterErrors())
                        .extracting(ParameterErrorResponseDto::parameter)
                        .containsExactly("timeId", "themeId");
                });
        }
    }

    @Nested
    class 예약_수정 {

        @Test
        void 예약을_수정한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time existingTime = timeWithId(1L);
            Time newTime = Time.reconstruct(2L, LocalTime.of(11, 0), null);
            Theme existingTheme = themeWithId(1L);
            Theme newTheme = Theme.reconstruct(2L, "새 테마", "새 설명", "https://example.com/new.png", null);
            Reservation existing = Reservation.reconstruct(
                1L, "예약자", futureDate, existingTime, existingTheme, ReservationStatus.ACTIVE);
            LocalDate newDate = futureDate.plusDays(1);
            ReservationUpdateCommand command = new ReservationUpdateCommand(newDate, 2L, 2L, 0L);
            givenReservation(1L, existing);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(newTime));
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(newTheme));

            // when
            ReservationCreateResponseDto result = reservationService.updateReservation(
                1L, "예약자", command);

            // then
            assertThat(result.date()).isEqualTo(newDate);
            assertThat(result.timeId()).isEqualTo(2L);
            assertThat(result.themeId()).isEqualTo(2L);
        }

        @Test
        void null인_선택_필드는_기존_값을_유지한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time existingTime = timeWithId(1L);
            Theme existingTheme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, "예약자", futureDate, existingTime, existingTheme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(null, null, null, 0L);
            givenReservation(1L, existing);

            // when
            ReservationCreateResponseDto result = reservationService.updateReservation(
                1L, "예약자", command);

            // then
            assertThat(result.date()).isEqualTo(futureDate);
            assertThat(result.timeId()).isEqualTo(1L);
            assertThat(result.themeId()).isEqualTo(1L);
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            ReservationUpdateCommand command = new ReservationUpdateCommand(
                LocalDate.now(fixedClock).plusDays(1), null, null, 0L);
            givenReservationNotFound(999L);

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(999L, "예약자", command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
        }

        @Test
        void 예약자_이름이_다르면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(futureDate, null, null, 0L);
            givenReservation(1L, existing);

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, "다른사람", command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 변경할 권한이 없습니다.");
        }

        @Test
        void 활성_예약이_아니면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation canceled = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.CANCELED);
            ReservationUpdateCommand command = new ReservationUpdateCommand(futureDate, null, null, 0L);
            givenReservation(1L, canceled);

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, "예약자", command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("활성된 예약이 아닙니다.");
        }

        @Test
        void 지난_날짜의_예약이면_예외가_발생한다() {
            // given
            LocalDate pastDate = LocalDate.now(fixedClock).minusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation past = Reservation.reconstruct(
                1L, "예약자", pastDate, time, theme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(pastDate, null, null, 0L);
            givenReservation(1L, past);

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, "예약자", command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 변경할 수 없습니다.");
        }

        @Test
        void 오늘_날짜라도_지난_시간으로는_예약을_수정할_수_없다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            LocalDate today = LocalDate.now(fixedClock);
            Time existingTime = timeWithId(1L);
            Time pastTime = timeWithId(2L, LocalTime.of(8, 0));
            Theme theme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, "예약자", futureDate, existingTime, theme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(today, 2L, null, 0L);
            givenReservation(1L, existing);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(pastTime));

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, "예약자", command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 변경할 수 없습니다.");
        }

        @Test
        void 변경할_timeId와_themeId가_존재하지_않으면_파라미터_에러를_모두_포함한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time existingTime = timeWithId(1L);
            Theme existingTheme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, "예약자", futureDate, existingTime, existingTheme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(null, 999L, 999L, 0L);
            givenReservation(1L, existing);
            when(timeRepository.findTimeByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());
            when(themeRepository.findThemeByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, "예약자", command))
                .isInstanceOfSatisfying(GeneralParametersException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo("수정할 자원이 존재하지 않습니다.");
                    assertThat(ex.getParameterErrors())
                        .extracting(ParameterErrorResponseDto::parameter)
                        .containsExactly("timeId", "themeId");
                });
        }

        @Test
        void 다른_예약과_날짜_시간_테마가_중복되면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation existing = Reservation.reconstruct(
                1L, "예약자", futureDate.plusDays(1), time, theme, ReservationStatus.ACTIVE);
            ReservationUpdateCommand command = new ReservationUpdateCommand(futureDate, null, null, 0L);
            givenReservation(1L, existing);
            doThrow(new DuplicateKeyException("duplicate"))
                .when(reservationRepository)
                .flush();

            // when & then
            assertThatThrownBy(() -> reservationService.updateReservation(1L, "예약자", command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 예약된 날짜, 시간, 테마입니다.");
        }
    }

    @Nested
    class 예약_취소 {

        @Test
        void 예약을_취소한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.ACTIVE);
            givenLockedReservation(1L, reservation);

            // when
            ReservationCancelResponseDto result = reservationService.cancelReservation(1L, "예약자");

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("예약자");
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            givenReservationLockNotFound(999L);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(999L, "예약자"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
        }

        @Test
        void 예약자_이름이_다르면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.ACTIVE);
            givenLockedReservation(1L, reservation);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, "다른사람"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 취소할 권한이 없습니다.");
        }

        @Test
        void 활성_예약이_아니면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation canceled = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.CANCELED);
            givenLockedReservation(1L, canceled);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, "예약자"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("활성된 예약이 아닙니다.");
        }

        @Test
        void 지난_날짜의_예약이면_예외가_발생한다() {
            // given
            LocalDate pastDate = LocalDate.now(fixedClock).minusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation past = Reservation.reconstruct(
                1L, "예약자", pastDate, time, theme, ReservationStatus.ACTIVE);
            givenLockedReservation(1L, past);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, "예약자"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 취소할 수 없습니다.");
        }

        @Test
        void 오늘_날짜라도_지난_시간의_예약이면_취소할_수_없다() {
            // given
            LocalDate today = LocalDate.now(fixedClock);
            Time pastTime = timeWithId(1L, LocalTime.of(8, 0));
            Theme theme = themeWithId(1L);
            Reservation past = Reservation.reconstruct(
                1L, "예약자", today, pastTime, theme, ReservationStatus.ACTIVE);
            givenLockedReservation(1L, past);

            // when & then
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, "예약자"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 취소할 수 없습니다.");
        }
    }

    @Nested
    class 예약_대기_취소 {

        @Test
        void 예약_대기를_취소한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation waiting = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.WAITING);
            when(reservationRepository.lockReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(waiting));

            // when
            ReservationCancelResponseDto result = reservationService.cancelWaitingReservation(
                1L, "예약자");

            // then
            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            when(reservationRepository.lockReservationByIdAndNotDeleted(999L))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.cancelWaitingReservation(999L, "예약자"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
        }

        @Test
        void 예약자_이름이_다르면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation waiting = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.WAITING);
            when(reservationRepository.lockReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(waiting));

            // when & then
            assertThatThrownBy(() -> reservationService.cancelWaitingReservation(1L, "다른사람"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 취소할 권한이 없습니다.");
        }

        @Test
        void 대기_예약이_아니면_예외가_발생한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation active = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.lockReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(active));

            // when & then
            assertThatThrownBy(() -> reservationService.cancelWaitingReservation(1L, "예약자"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("대기중인 예약이 아닙니다.");
        }

        @Test
        void 지난_날짜의_대기_예약이면_예외가_발생한다() {
            // given
            LocalDate pastDate = LocalDate.now(fixedClock).minusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation pastWaiting = Reservation.reconstruct(
                1L, "예약자", pastDate, time, theme, ReservationStatus.WAITING);
            when(reservationRepository.lockReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(pastWaiting));

            // when & then
            assertThatThrownBy(() -> reservationService.cancelWaitingReservation(1L, "예약자"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 취소할 수 없습니다.");
        }

        @Test
        void 오늘_날짜라도_지난_시간의_대기_예약이면_취소할_수_없다() {
            // given
            LocalDate today = LocalDate.now(fixedClock);
            Time pastTime = timeWithId(1L, LocalTime.of(8, 0));
            Theme theme = themeWithId(1L);
            Reservation pastWaiting = Reservation.reconstruct(
                1L, "예약자", today, pastTime, theme, ReservationStatus.WAITING);
            when(reservationRepository.lockReservationByIdAndNotDeleted(1L))
                .thenReturn(Optional.of(pastWaiting));

            // when & then
            assertThatThrownBy(() -> reservationService.cancelWaitingReservation(1L, "예약자"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("지난 예약은 취소할 수 없습니다.");
        }
    }

    @Nested
    class 예약_삭제 {

        @Test
        void 예약을_삭제한다() {
            // given
            LocalDate futureDate = LocalDate.now(fixedClock).plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, "예약자", futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(reservation));

            // when
            reservationService.deleteReservationById(1L);
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            when(reservationRepository.findByIdAndDeletedAtIsNull(999L))
                .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.deleteReservationById(999L))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
        }
    }

    private void givenLockedReservation(Long id, Reservation reservation) {
        when(reservationRepository.lockReservationByIdAndNotDeleted(id))
            .thenReturn(Optional.of(reservation));
    }

    private void givenReservationLockNotFound(Long id) {
        when(reservationRepository.lockReservationByIdAndNotDeleted(id))
            .thenReturn(Optional.empty());
    }

    private void givenReservation(Long id, Reservation reservation) {
        when(reservationRepository.findByIdAndDeletedAtIsNull(id))
            .thenReturn(Optional.of(reservation));
    }

    private void givenReservationNotFound(Long id) {
        when(reservationRepository.findByIdAndDeletedAtIsNull(id))
            .thenReturn(Optional.empty());
    }
}
