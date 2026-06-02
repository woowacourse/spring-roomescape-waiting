package roomescape.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import roomescape.domain.ReservationTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ReservationTimeServiceTest {

    private final ReservationTimeRepository reservationTimeRepository = mock();
    private final ReservationRepository reservationRepository = mock();
    private final ReservationTimeService service = new ReservationTimeService(reservationTimeRepository, reservationRepository);

    @Test
    void 전체_시간_조회_테스트() {
        // given
        List<ReservationTime> times = List.of(
                new ReservationTime(1L, LocalTime.of(8, 0)),
                new ReservationTime(2L, LocalTime.of(10, 0)));
        when(reservationTimeRepository.findAll())
                .thenReturn(times);

        // when
        List<ReservationTime> result = service.findAll();

        // then
        assertThat(result).isEqualTo(times);
        verify(reservationTimeRepository, times(1)).findAll();
        verifyNoMoreInteractions(reservationTimeRepository, reservationRepository);
    }

    @Test
    void 시간_생성_테스트() {
        // given
        Long id = 1L;
        LocalTime startAt = LocalTime.of(8, 0);
        ReservationTime time = new ReservationTime(id, startAt);

        when(reservationTimeRepository.insert(any(ReservationTime.class)))
                .thenReturn(time);

        // when
        ReservationTime result = service.create(startAt);

        // then
        ArgumentCaptor<ReservationTime> captor = ArgumentCaptor.forClass(ReservationTime.class);

        assertAll(
                () -> assertThat(result.getId()).isEqualTo(id),
                () -> assertThat(result.getStartAt()).isEqualTo(startAt));

        verify(reservationTimeRepository, times(1)).insert(captor.capture());
        ReservationTime captured = captor.getValue();

        assertAll(
                () -> assertThat(captured.getId()).isNull(),
                () -> assertThat(captured.getStartAt()).isEqualTo(startAt));

        verifyNoMoreInteractions(reservationTimeRepository, reservationRepository);
    }

    @Test
    void 시간_삭제_테스트() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByTimeId(id))
                .thenReturn(false);

        // when
        service.delete(id);

        // then
        verify(reservationRepository, times(1)).existsByTimeId(id);
        verify(reservationTimeRepository, times(1)).delete(id);
        verifyNoMoreInteractions(reservationTimeRepository, reservationRepository);
    }

    @Test
    void 예약이_존재하는_시간은_삭제시_예외_발생() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByTimeId(id))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_IN_USE)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");

        verify(reservationRepository, times(1)).existsByTimeId(id);
        verify(reservationTimeRepository, never()).delete(anyLong());
        verifyNoMoreInteractions(reservationTimeRepository, reservationRepository);
    }

    @Test
    void 삭제_중_예약이_생긴_시간은_삭제시_예외_발생() {
        // given
        Long id = 1L;
        when(reservationRepository.existsByTimeId(id))
                .thenReturn(false);
        doThrow(new DataIntegrityViolationException("referenced time"))
                .when(reservationTimeRepository)
                .delete(id);

        // when & then
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_IN_USE)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");

        verify(reservationRepository, times(1)).existsByTimeId(id);
        verify(reservationTimeRepository, times(1)).delete(id);
        verifyNoMoreInteractions(reservationTimeRepository, reservationRepository);
    }
}
