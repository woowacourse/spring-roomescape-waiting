package roomescape.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.schedule.ScheduleRepository;
import roomescape.booking.schedule.ScheduleService;
import roomescape.booking.schedule.dto.ScheduleRequest;
import roomescape.booking.schedule.dto.ScheduleResponse;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeService;
import roomescape.theme.Theme;
import roomescape.theme.ThemeService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    private ScheduleService scheduleService;
    private ScheduleRepository scheduleRepository;
    private ReservationTimeService reservationTimeService;
    private ThemeService themeService;

    private ScheduleRequest REQUEST;
    private ReservationTime RESERVATION_TIME;
    private Theme THEME;

    @BeforeEach
    void setUp() {
        REQUEST = new ScheduleRequest(LocalDate.now().plusDays(1), 1L, 1L);
        RESERVATION_TIME = reservationTimeWithId(REQUEST.reservationTimeId(), new ReservationTime(LocalTime.of(12, 40)));
        THEME = themeWithId(REQUEST.themeId(), new Theme("테마명", "테마 설명", "썸네일 URL"));

        scheduleRepository = mock(ScheduleRepository.class);
        reservationTimeService = mock(ReservationTimeService.class);
        themeService = mock(ThemeService.class);
        scheduleService = new ScheduleService(scheduleRepository, reservationTimeService, themeService);
    }

    @Test
    @DisplayName("스케줄을 생성할 수 있다.")
    void create() {
        // given
        given(reservationTimeService.findById(REQUEST.reservationTimeId()))
                .willReturn(RESERVATION_TIME);
        given(themeService.findById(REQUEST.themeId()))
                .willReturn(THEME);

        Schedule schedule = new Schedule(REQUEST.date(), RESERVATION_TIME, THEME);
        Schedule savedSchedule = scheduleWithId(1L, schedule);
        given(scheduleRepository.save(any(Schedule.class)))
                .willReturn(savedSchedule);

        // when
        ScheduleResponse response = scheduleService.create(REQUEST);

        // then
        assertThat(response.id()).isEqualTo(savedSchedule.getId());
    }
}
