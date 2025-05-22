package roomescape.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsThemeException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsTimeException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepository;
import roomescape.schedule.dto.ScheduleRequest;
import roomescape.schedule.dto.ScheduleResponse;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    private ScheduleService scheduleService;
    private ScheduleRepository scheduleRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;

    private ScheduleRequest REQUEST;
    private Optional<ReservationTime> RESERVATION_TIME;
    private Optional<Theme> THEME;

    @BeforeEach
    void setUp() {
        REQUEST = new ScheduleRequest(LocalDate.now().plusDays(1), 1L, 1L);
        RESERVATION_TIME = Optional.of(reservationTimeWithId(REQUEST.reservationTimeId(), new ReservationTime(LocalTime.of(12, 40))));
        THEME = Optional.of(themeWithId(REQUEST.themeId(), new Theme("테마명", "테마 설명", "썸네일 URL")));

        scheduleRepository = mock(ScheduleRepository.class);
        reservationTimeRepository = mock(ReservationTimeRepository.class);
        themeRepository = mock(ThemeRepository.class);
        scheduleService = new ScheduleService(scheduleRepository, reservationTimeRepository, themeRepository);
    }

    @Test
    @DisplayName("스케줄을 생성할 수 있다.")
    void create() {
        // given
        given(reservationTimeRepository.findById(REQUEST.reservationTimeId()))
                .willReturn(RESERVATION_TIME);
        given(themeRepository.findById(REQUEST.themeId()))
                .willReturn(THEME);

        Schedule schedule = new Schedule(REQUEST.date(), RESERVATION_TIME.get(), THEME.get());
        Schedule savedSchedule = scheduleWithId(1L, schedule);
        given(scheduleRepository.save(any(Schedule.class)))
                .willReturn(savedSchedule);

        // when
        ScheduleResponse response = scheduleService.create(REQUEST);

        // then
        assertThat(response.id()).isEqualTo(savedSchedule.getId());
    }

    @Test
    @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
    void createWithNonExistentTime() {
        // given
        given(reservationTimeRepository.findById(REQUEST.reservationTimeId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.create(REQUEST))
                .isInstanceOf(ReservationNotExistsTimeException.class);
    }

    @Test
    @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
    void createWithNonExistentTheme() {
        // given
        given(reservationTimeRepository.findById(REQUEST.reservationTimeId()))
                .willReturn(RESERVATION_TIME);
        given(themeRepository.findById(REQUEST.themeId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.create(REQUEST))
                .isInstanceOf(ReservationNotExistsThemeException.class);
    }
}
