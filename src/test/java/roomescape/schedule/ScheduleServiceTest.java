package roomescape.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeService;
import roomescape.schedule.dto.ScheduleRequest;
import roomescape.schedule.dto.ScheduleResponse;
import roomescape.theme.Theme;
import roomescape.theme.ThemeService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ReservationTimeService reservationTimeService;
    @Mock
    private ThemeService themeService;
    @InjectMocks
    private ScheduleService scheduleService;

    private ScheduleRequest request;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void setUp() {
        request = new ScheduleRequest(LocalDate.now().plusDays(1), 1L, 1L);
        reservationTime = reservationTimeWithId(request.reservationTimeId(), new ReservationTime(LocalTime.of(12, 40)));
        theme = themeWithId(request.themeId(), new Theme("테마명", "테마 설명", "썸네일 URL"));
    }

    @Test
    @DisplayName("스케줄을 생성할 수 있다.")
    void create() {
        // given
        given(reservationTimeService.getById(request.reservationTimeId()))
                .willReturn(reservationTime);
        given(themeService.getById(request.themeId()))
                .willReturn(theme);

        Schedule schedule = new Schedule(request.date(), reservationTime, theme);
        Schedule savedSchedule = scheduleWithId(1L, schedule);
        given(scheduleRepository.save(any(Schedule.class)))
                .willReturn(savedSchedule);

        // when
        ScheduleResponse response = scheduleService.create(request);

        // then
        assertThat(response.id()).isEqualTo(savedSchedule.getId());
    }
}
