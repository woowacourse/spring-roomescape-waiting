package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.RoomEscapeException;
import roomescape.reservationtime.application.dto.AvailableReservationTimeQueryResult;
import roomescape.reservationtime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationtime.application.service.ReservationTimeService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.AvailableReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository timeRepository;

    @InjectMocks
    private ReservationTimeService timeService;

    @DisplayName("날짜/테마 선택 시 예약 가능한 시간 조회를 테스트합니다.")
    @Test
    void find_available_times_by_date_and_theme() {
        when(timeRepository.findAvailableByThemeAndDate(1L, LocalDate.of(2026, 5, 6))).thenReturn(List.of(
                new AvailableReservationTime(1L, LocalTime.of(9, 0), true),
                new AvailableReservationTime(2L, LocalTime.of(10, 0), true)
        ));

        List<AvailableReservationTimeQueryResult> result = timeService.findAvailableTimes(1L, LocalDate.of(2026, 5, 6));

        assertThat(result).containsExactly(
                new AvailableReservationTimeQueryResult(1L, LocalTime.of(9, 0), true),
                new AvailableReservationTimeQueryResult(2L, LocalTime.of(10, 0), true)
        );
    }

    @DisplayName("예약이 이미 되어있을 때 예약 가능한 시간 조회를 테스트합니다.")
    @Test
    void find_available_times_if_one_reserved() {
        when(timeRepository.findAvailableByThemeAndDate(1L, LocalDate.of(2026, 5, 6))).thenReturn(List.of(
                new AvailableReservationTime(1L, LocalTime.of(9, 0), false),
                new AvailableReservationTime(2L, LocalTime.of(10, 0), true)
        ));

        List<AvailableReservationTimeQueryResult> result = timeService.findAvailableTimes(1L, LocalDate.of(2026, 5, 6));

        assertThat(result).containsExactly(
                new AvailableReservationTimeQueryResult(1L, LocalTime.of(9, 0), false),
                new AvailableReservationTimeQueryResult(2L, LocalTime.of(10, 0), true)
        );
    }

    @DisplayName("중복된 타임 추가 시 예외 발생을 테스트합니다.")
    @Test
    void save_duplicated_time_exception() {
        when(timeRepository.existsByStartAt(LocalTime.of(9, 0))).thenReturn(true);

        assertThatThrownBy(() -> timeService.save(new ReservationTimeCreateCommand(LocalTime.of(9, 0))))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("시간 09:00이(가) 이미 존재합니다.");
    }
}
