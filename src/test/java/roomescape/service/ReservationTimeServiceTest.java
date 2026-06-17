package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.custom.CannotReadPastReservationTimeAvailability;
import roomescape.exception.custom.ReservationTimeAlreadyExistsException;
import roomescape.exception.custom.ReservationTimeNotExistsException;
import roomescape.repository.ReservationTimeRepository;

public class ReservationTimeServiceTest {
    private ReservationTimeService reservationTimeService;
    private ReservationTimeRepository reservationTimeRepository;
    private Clock clock;

    @BeforeEach
    void beforeEach() {
        reservationTimeRepository = Mockito.mock(ReservationTimeRepository.class);
        clock = Clock.fixed(Instant.parse("2026-05-02T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, clock);
    }

    @Test
    void saveTest() {
        ReservationTime reservationTimeWithoutId = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime reservationTime = ReservationTime.withId(1L, reservationTimeWithoutId);

        when(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).thenReturn(false);
        when(reservationTimeRepository.save(reservationTimeWithoutId)).thenReturn(reservationTime);

        assertThat(reservationTimeService.save(reservationTimeWithoutId)).isEqualTo(reservationTime);
    }

    @Test
    void saveExceptionTest() {
        ReservationTime reservationTimeWithoutId = new ReservationTime(LocalTime.of(10, 0));

        when(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).thenReturn(true);

        assertThatThrownBy(() -> reservationTimeService.save(reservationTimeWithoutId))
                .isInstanceOf(ReservationTimeAlreadyExistsException.class);
    }

    @Test
    void findAllTest() {
        List<ReservationTime> reservationTimes = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0))
        );

        when(reservationTimeRepository.findAll()).thenReturn(reservationTimes);

        assertThat(reservationTimeService.findAll()).isEqualTo(reservationTimes);
    }

    @Test
    void findReservedTimesByDateAndThemeTest() {
        LocalDate date = LocalDate.of(2026, 5, 3);
        Theme theme = new Theme(1L, "피즈의 모험", "모험 이야기", "url.jpg");

        List<ReservationTime> reservedTimes = List.of(
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new ReservationTime(2L, LocalTime.of(11, 0)),
                new ReservationTime(3L, LocalTime.of(12, 0))
        );

        when(reservationTimeRepository.findReservedTimesByDateAndTheme_Id(date, theme.getId())).thenReturn(
                reservedTimes);
        assertThat(reservationTimeService.findReservedTimesByDateAndTheme(date, theme.getId())).isEqualTo(
                reservedTimes);
    }

    @Test
    void findAvailabilityByDateAndThemeExceptionTest() {
        LocalDate date = LocalDate.of(2026, 5, 1);

        assertThatThrownBy(() -> reservationTimeService.findReservedTimesByDateAndTheme(date, 1L))
                .isInstanceOf(CannotReadPastReservationTimeAvailability.class);
    }

    @Test
    void deleteTest() {
        reservationTimeService.delete(1L);

        verify(reservationTimeRepository, times(1)).deleteById(1L);
    }

    @Test
    void findReservationTimeTest() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));

        assertThat(reservationTimeService.findReservationTime(1L)).isEqualTo(reservationTime);
    }

    @Test
    void findReservationTimeExceptionTest() {
        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationTimeService.findReservationTime(1L))
                .isInstanceOf(ReservationTimeNotExistsException.class);
    }
}
