package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.time.dao.TimeDao;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.TimeCreateRequest;
import roomescape.time.dto.TimeResponse;

@ExtendWith(MockitoExtension.class)
class TimeServiceTest {
    @Mock
    private TimeDao timeDao;
    @InjectMocks
    private TimeService timeService;

    @DisplayName("예약 시간을 모두 조회할 수 있다.")
    @Test
    void findTimesTest() {
        given(timeDao.findTimes()).willReturn(List.of(
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new ReservationTime(2L, LocalTime.of(10, 0))));
        List<TimeResponse> expected = List.of(
                new TimeResponse(1L, LocalTime.of(19, 0)),
                new TimeResponse(2L, LocalTime.of(10, 0)));

        List<TimeResponse> actual = timeService.findTimes();

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예약 시간의 예약 가능 여부를 조회할 수 있다.")
    @Test
    void findAvailableTimesTest() {
        LocalDate date = LocalDate.of(2023, 8, 20);
        Long themeId = 1L;
        given(timeDao.findTimes()).willReturn(List.of(
                new ReservationTime(1L, LocalTime.of(19, 0)),
                new ReservationTime(2L, LocalTime.of(10, 0))));
        given(timeDao.findTimesExistsReservationDateAndThemeId(date, themeId)).willReturn(List.of(
                new ReservationTime(1L, LocalTime.of(19, 0))));
        List<AvailableTimeResponse> expected = List.of(
                new AvailableTimeResponse(new TimeResponse(1L, LocalTime.of(19, 0)), true),
                new AvailableTimeResponse(new TimeResponse(2L, LocalTime.of(10, 0)), false));

        List<AvailableTimeResponse> actual = timeService.findAvailableTimes(date, themeId);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예약 시간을 생성할 수 있다.")
    @Test
    void createTimeTest() {
        TimeCreateRequest request = new TimeCreateRequest(LocalTime.of(19, 0));
        given(timeDao.createTime(request.createReservationTime()))
                .willReturn(new ReservationTime(1L, LocalTime.of(19, 0)));
        TimeResponse expected = new TimeResponse(1L, LocalTime.of(19, 0));

        TimeResponse actual = timeService.createTime(request);

        assertThat(actual).isEqualTo(expected);
    }
}
