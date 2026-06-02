package roomescape.service;

import org.junit.jupiter.api.Test;

import roomescape.domain.ReservationTime;
import roomescape.domain.TimeAvailability;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ReservationAvailabilityServiceTest {

    private final ReservationTimeRepository reservationTimeRepository = mock();
    private final ThemeRepository themeRepository = mock();
    private final ReservationAvailabilityService service = new ReservationAvailabilityService(
            reservationTimeRepository,
            themeRepository);

    private final LocalDate date = LocalDate.now().plusDays(1);

    @Test
    void 예약_가능한_시간을_조회한다() {
        // given
        Long themeId = 1L;
        ReservationTime reservedTime = new ReservationTime(1L, LocalTime.parse("08:00"));
        ReservationTime availableTime = new ReservationTime(2L, LocalTime.parse("10:00"));
        List<TimeAvailability> timeAvailabilities = List.of(
                new TimeAvailability(reservedTime, false),
                new TimeAvailability(availableTime, true));

        when(themeRepository.existsById(themeId))
                .thenReturn(true);
        when(reservationTimeRepository.findAvailabilitiesByThemeIdAndDate(themeId, date))
                .thenReturn(timeAvailabilities);

        // when
        List<TimeAvailability> result = service.findAvailableTimes(themeId, date);

        // then
        assertThat(result).extracting(TimeAvailability::isAvailable)
                .containsExactly(false, true);
        verify(themeRepository, times(1)).existsById(themeId);
        verify(reservationTimeRepository, times(1)).findAvailabilitiesByThemeIdAndDate(themeId, date);
        verifyNoMoreInteractions(reservationTimeRepository, themeRepository);
    }

    @Test
    void 존재하지_않는_테마의_예약_가능_시간_조회시_예외_발생() {
        // given
        Long themeId = 1L;
        when(themeRepository.existsById(themeId))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> service.findAvailableTimes(themeId, date))
                .isInstanceOf(RoomescapeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .hasMessage("존재하지 않는 테마입니다.");

        verify(themeRepository, times(1)).existsById(themeId);
        verifyNoMoreInteractions(reservationTimeRepository, themeRepository);
    }
}
