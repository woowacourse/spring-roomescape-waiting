package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.result.ReservationWaitingOrderResult;
import roomescape.service.result.WaitingResult;

class ReservationWaitingServiceTest {

    private final ReservationWaitingRepository reservationWaitingRepository = mock();
    private final JpaReservationTimeRepository reservationTimeRepository = mock();
    private final JpaThemeRepository themeRepository = mock();
    private final ReservationWaitingValidator reservationWaitingValidator = mock();
    private final ReservationWaitingService service = new ReservationWaitingService(
            reservationWaitingRepository,
            reservationTimeRepository,
            themeRepository,
            reservationWaitingValidator);

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final ReservationTime time = new ReservationTime(1L, LocalTime.parse("08:00"));
    private final Theme theme = new Theme(1L, "테스트 테마", "테마 설명", "썸네일 주소");

    @Test
    void 이름으로_예약_대기_목록을_조회한다() {
        // given
        String name = "브라운";
        List<ReservationWaiting> waitings = List.of(
                new ReservationWaiting(1L, name, date, time, theme),
                new ReservationWaiting(2L, name, date.plusDays(1), time, theme));

        when(reservationWaitingRepository.findByName(name))
                .thenReturn(waitings);
        when(reservationWaitingRepository.findOrderResultsBy(waitings))
                .thenReturn(List.of(
                        new ReservationWaitingOrderResult(
                                3L,
                                date,
                                time.getId(),
                                theme.getId(),
                                LocalDateTime.of(date, LocalTime.parse("07:50"))),
                        new ReservationWaitingOrderResult(
                                1L,
                                date,
                                time.getId(),
                                theme.getId(),
                                LocalDateTime.of(date, LocalTime.parse("08:00"))),
                        new ReservationWaitingOrderResult(
                                2L,
                                date.plusDays(1),
                                time.getId(),
                                theme.getId(),
                                LocalDateTime.of(date.plusDays(1), LocalTime.parse("08:00")))));

        // when
        List<WaitingResult> result = service.findByName(name);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(WaitingResult::id)
                        .containsExactly(1L, 2L),
                () -> assertThat(result).extracting(WaitingResult::name)
                        .containsExactly(name, name),
                () -> assertThat(result).extracting(WaitingResult::date)
                        .containsExactly(date, date.plusDays(1)),
                () -> assertThat(result).extracting(WaitingResult::turn)
                        .containsExactly(2L, 1L));
    }

    @Test
    void 사용자_예약_대기를_생성한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        ReservationWaiting savedWaiting = new ReservationWaiting(id, name, date, time, theme);

        when(reservationTimeRepository.findById(time.getId()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(theme.getId()))
                .thenReturn(Optional.of(theme));
        when(reservationWaitingRepository.insert(any(ReservationWaiting.class)))
                .thenReturn(id);
        when(reservationWaitingRepository.findById(id))
                .thenReturn(Optional.of(savedWaiting));
        when(reservationWaitingRepository.countEarlierWaitings(id))
                .thenReturn(0L);

        // when
        WaitingResult result = service.create(name, date, time.getId(), theme.getId());

        // then
        ArgumentCaptor<ReservationWaiting> captor = ArgumentCaptor.forClass(ReservationWaiting.class);
        verify(reservationWaitingRepository).insert(captor.capture());

        ReservationWaiting captured = captor.getValue();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(id),
                () -> assertThat(result.name()).isEqualTo(name),
                () -> assertThat(result.date()).isEqualTo(date),
                () -> assertThat(result.time()).isEqualTo(time),
                () -> assertThat(result.theme()).isEqualTo(theme),
                () -> assertThat(result.turn()).isEqualTo(1L),
                () -> assertThat(captured.getId()).isNull(),
                () -> assertThat(captured.getName()).isEqualTo(name),
                () -> assertThat(captured.getDate()).isEqualTo(date),
                () -> assertThat(captured.getTime()).isEqualTo(time),
                () -> assertThat(captured.getTheme()).isEqualTo(theme));
    }

    @Test
    void 예약_대기_검증_실패시_저장하지_않는다() {
        // given
        String name = "브라운";
        when(reservationTimeRepository.findById(time.getId()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(theme.getId()))
                .thenReturn(Optional.of(theme));
        doThrow(new BusinessException(ErrorCode.INVALID_INPUT, "예약 가능한 시간에는 대기를 신청할 수 없습니다."))
                .when(reservationWaitingValidator).validateWaiting(any(ReservationWaiting.class));

        // when & then
        assertThatThrownBy(() -> service.create(name, date, time.getId(), theme.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("예약 가능한 시간에는 대기를 신청할 수 없습니다.");

        verify(reservationWaitingRepository, never()).insert(any(ReservationWaiting.class));
    }

    @Test
    void 예약_대기_저장시_중복키_예외가_발생하면_비즈니스_예외로_변환한다() {
        // given
        String name = "브라운";
        when(reservationTimeRepository.findById(time.getId()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(theme.getId()))
                .thenReturn(Optional.of(theme));
        when(reservationWaitingRepository.insert(any(ReservationWaiting.class)))
                .thenThrow(new DuplicateKeyException("duplicate waiting"));

        // when & then
        assertThatThrownBy(() -> service.create(name, date, time.getId(), theme.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 예약 대기를 신청한 시간입니다.");
    }

    @Test
    void 존재하지_않는_시간으로_예약_대기_신청시_예외_발생() {
        // given
        Long timeId = 999L;
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.create("브라운", date, timeId, theme.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 시간입니다.");

        verify(reservationWaitingRepository, never()).insert(any(ReservationWaiting.class));
    }

    @Test
    void 존재하지_않는_테마로_예약_대기_신청시_예외_발생() {
        // given
        Long themeId = 999L;
        when(reservationTimeRepository.findById(time.getId()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.create("브라운", date, time.getId(), themeId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 테마입니다.");

        verify(reservationWaitingRepository, never()).insert(any(ReservationWaiting.class));
    }

    @Test
    void 사용자_본인_예약_대기를_삭제한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        ReservationWaiting waiting = new ReservationWaiting(id, name, date, time, theme);
        when(reservationWaitingRepository.findById(id))
                .thenReturn(Optional.of(waiting));

        // when
        service.delete(id, name);

        // then
        verify(reservationWaitingRepository).delete(id);
    }

    @Test
    void 다른_사용자의_예약_대기_삭제시_예외_발생() {
        // given
        Long id = 1L;
        ReservationWaiting waiting = new ReservationWaiting(id, "브라운", date, time, theme);
        when(reservationWaitingRepository.findById(id))
                .thenReturn(Optional.of(waiting));
        doThrow(new BusinessException(ErrorCode.FORBIDDEN_RESERVATION, "본인의 예약 대기만 취소할 수 있습니다."))
                .when(reservationWaitingValidator).validateUpdatableReservation(waiting, "구구");

        // when & then
        assertThatThrownBy(() -> service.delete(id, "구구"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("본인의 예약 대기만 취소할 수 있습니다.");

        verify(reservationWaitingRepository, never()).delete(id);
    }

    @Test
    void 지난_예약_대기_삭제시_예외_발생() {
        // given
        Long id = 1L;
        String name = "브라운";
        ReservationWaiting waiting = new ReservationWaiting(
                id,
                name,
                LocalDate.now().minusDays(1),
                time,
                theme);
        when(reservationWaitingRepository.findById(id))
                .thenReturn(Optional.of(waiting));
        doThrow(new BusinessException(ErrorCode.PAST_RESERVATION_LOCKED, "이미 지난 예약 대기는 취소할 수 없습니다."))
                .when(reservationWaitingValidator).validateUpdatableReservation(waiting, name);

        // when & then
        assertThatThrownBy(() -> service.delete(id, name))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 지난 예약 대기는 취소할 수 없습니다.");

        verify(reservationWaitingRepository, never()).delete(id);
    }

    @Test
    void 존재하지_않는_예약_대기_삭제시_예외_발생() {
        // given
        Long id = 999L;
        when(reservationWaitingRepository.findById(id))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.delete(id, "브라운"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("존재하지 않는 예약 대기입니다.");

        verify(reservationWaitingRepository, never()).delete(id);
    }
}
