package roomescape.reservationtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.ReservationRepository;
import roomescape.reservationtime.application.ReservationTimeService;
import roomescape.reservationtime.dto.request.ReservationTimeSaveRequest;
import roomescape.reservationtime.dto.response.ReservationTimeFindResponse;
import roomescape.reservationtime.dto.response.ReservationTimeSaveResponse;
import roomescape.schedule.application.ScheduleService;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationTimeServiceTest {
    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("매니저는 시간 슬롯을 저장할 수 있다.")
    void save_성공_테스트() {
        ReservationTimeSaveRequest request = new ReservationTimeSaveRequest(LocalTime.of(14, 0));
        ReservationTime saved = new ReservationTime(5L, LocalTime.of(14, 0));

        when(reservationTimeRepository.existsAlreadyTime(request.startAt())).thenReturn(false);
        when(reservationTimeRepository.save(request.toDomain())).thenReturn(saved);

        ReservationTimeSaveResponse response = reservationTimeService.save(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.startAt()).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    @DisplayName("매니저는 전체 시간 슬롯을 조회할 수 있다.")
    void findAll_성공_테스트() {
        List<ReservationTime> reservationTimes = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );
        when(reservationTimeRepository.findAll()).thenReturn(reservationTimes);

        List<ReservationTimeFindResponse> response = reservationTimeService.findAll();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(1).id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("스케줄에 시간에 대한 참조가 존재하면 시간 삭제에 실패한다.")
    void delete_실패_테스트_1() {
        // given
        long timeId = 1L;
        doThrow(new EscapeRoomException(ErrorCode.SCHEDULE_TIME_IN_USE, timeId))
            .when(scheduleService).validateTimeDeletable(timeId);

        // when, then
        assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                .isInstanceOfSatisfying(EscapeRoomException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_TIME_IN_USE);
                });

        verify(reservationTimeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("스케줄에 시간에 대한 참조가 존재하지 않으면 시간 삭제에 성공한다.")
    void delete_성공_테스트() {
        // given
        long timeId = 1L;

        // when
        reservationTimeService.delete(timeId);

        // then
        verify(reservationTimeRepository).deleteById(anyLong());
    }
}
