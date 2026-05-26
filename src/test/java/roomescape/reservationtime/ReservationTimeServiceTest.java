package roomescape.reservationtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservationtime.application.ReservationTimeService;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.schedule.application.ScheduleService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReservationTimeServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    @DisplayName("스케줄에 시간에 대한 참조가 존재하면 시간 삭제에 실패한다.")
    void delete_실패_테스트_1() {
        // given
        long timeId = 1L;
        doThrow(new IllegalStateException()).when(scheduleService).validateTimeDeletable(timeId);

        // when, then
        assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                .isInstanceOf(IllegalStateException.class);

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
