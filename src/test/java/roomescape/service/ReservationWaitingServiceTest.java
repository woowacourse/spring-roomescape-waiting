package roomescape.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.WaitingWithTurn;
import roomescape.service.result.WaitingResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationWaitingServiceTest {

    private final ReservationWaitingRepository reservationWaitingRepository = mock();
    private final ReservationTimeRepository reservationTimeRepository = mock();
    private final ThemeRepository themeRepository = mock();
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
        List<WaitingWithTurn> waitingWithTurns = List.of(
                new WaitingWithTurn(new ReservationWaiting(1L, name, date, time, theme), 1L),
                new WaitingWithTurn(new ReservationWaiting(2L, name, date.plusDays(1), time, theme), 2L));

        when(reservationWaitingRepository.findByNameWithTurn(name))
                .thenReturn(waitingWithTurns);

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
                        .containsExactly(1L, 2L));
    }

    @Test
    void 사용자_예약_대기를_생성한다() {
        // given
        Long id = 1L;
        String name = "브라운";
        ReservationWaiting savedWaiting = new ReservationWaiting(id, name, date, time, theme);

        when(reservationTimeRepository.findBy(time.getId()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findBy(theme.getId()))
                .thenReturn(Optional.of(theme));
        when(reservationWaitingRepository.insert(any(ReservationWaiting.class)))
                .thenReturn(id);
        when(reservationWaitingRepository.findByIdWithTurn(id))
                .thenReturn(Optional.of(new WaitingWithTurn(savedWaiting, 1L)));

        // when
        WaitingResult result = service.create(name, date, time.getId(), theme.getId());

        // then
        ArgumentCaptor<ReservationWaiting> captor = ArgumentCaptor.forClass(ReservationWaiting.class);
        verify(reservationWaitingValidator).validateWaiting(any(ReservationWaiting.class));
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
        when(reservationTimeRepository.findBy(time.getId()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findBy(theme.getId()))
                .thenReturn(Optional.of(theme));
        doThrow(new RoomescapeException(ErrorCode.INVALID_INPUT, "예약 가능한 시간에는 대기를 신청할 수 없습니다."))
                .when(reservationWaitingValidator).validateWaiting(any(ReservationWaiting.class));

        // when & then
        assertThatThrownBy(() -> service.create(name, date, time.getId(), theme.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT)
                .hasMessage("예약 가능한 시간에는 대기를 신청할 수 없습니다.");

        verify(reservationWaitingRepository, never()).insert(any(ReservationWaiting.class));
    }

    @Test
    void 동시에_중복_대기_신청시_예외_발생() {
        // given
        String name = "브라운";
        when(reservationTimeRepository.findBy(time.getId()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findBy(theme.getId()))
                .thenReturn(Optional.of(theme));
        when(reservationWaitingRepository.insert(any(ReservationWaiting.class)))
                .thenThrow(new DuplicateKeyException("duplicate waiting"));

        // when & then
        assertThatThrownBy(() -> service.create(name, date, time.getId(), theme.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_RESOURCE)
                .hasMessage("이미 예약 대기를 신청한 시간입니다.");
    }

    @Test
    void 존재하지_않는_시간으로_예약_대기_신청시_예외_발생() {
        // given
        Long timeId = 999L;
        when(reservationTimeRepository.findBy(timeId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.create("브라운", date, timeId, theme.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .hasMessage("존재하지 않는 시간입니다.");

        verify(reservationWaitingRepository, never()).insert(any(ReservationWaiting.class));
    }

    @Test
    void 존재하지_않는_테마로_예약_대기_신청시_예외_발생() {
        // given
        Long themeId = 999L;
        when(reservationTimeRepository.findBy(time.getId()))
                .thenReturn(Optional.of(time));
        when(themeRepository.findBy(themeId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.create("브라운", date, time.getId(), themeId))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
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
        verify(reservationWaitingValidator).validateUpdatableReservation(waiting, name);
        verify(reservationWaitingRepository).delete(id);
    }

    @Test
    void 다른_사용자의_예약_대기_삭제시_예외_발생() {
        // given
        Long id = 1L;
        ReservationWaiting waiting = new ReservationWaiting(id, "브라운", date, time, theme);
        when(reservationWaitingRepository.findById(id))
                .thenReturn(Optional.of(waiting));
        doThrow(new RoomescapeException(ErrorCode.FORBIDDEN_RESOURCE, "본인의 예약 대기만 취소할 수 있습니다."))
                .when(reservationWaitingValidator).validateUpdatableReservation(waiting, "구구");

        // when & then
        assertThatThrownBy(() -> service.delete(id, "구구"))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_RESOURCE)
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
        doThrow(new RoomescapeException(ErrorCode.PAST_RESOURCE_LOCKED, "이미 지난 예약 대기는 취소할 수 없습니다."))
                .when(reservationWaitingValidator).validateUpdatableReservation(waiting, name);

        // when & then
        assertThatThrownBy(() -> service.delete(id, name))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAST_RESOURCE_LOCKED)
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
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .hasMessage("존재하지 않는 예약 대기입니다.");

        verify(reservationWaitingRepository, never()).delete(id);
    }
}
