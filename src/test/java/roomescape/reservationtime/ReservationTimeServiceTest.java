package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.repository.JpaReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.Theme;
import roomescape.theme.service.ThemeService;

class ReservationTimeServiceTest {

    @Test
    @DisplayName("예약 시간을 저장한다")
    void save() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );
        ReservationTime savedTime = ReservationTime.of(1L, LocalTime.parse("10:00"));

        when(reservationTimeRepository.existsByStartAt(LocalTime.parse("10:00"))).thenReturn(false);
        when(reservationTimeRepository.save(any(ReservationTime.class))).thenReturn(savedTime);

        ReservationTime saved = reservationTimeService.save(LocalTime.parse("10:00"));

        assertThat(saved).isEqualTo(savedTime);
    }

    @Test
    @DisplayName("중복된 예약 시간은 저장할 수 없다")
    void saveDuplicateTime() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );

        when(reservationTimeRepository.existsByStartAt(LocalTime.parse("10:00"))).thenReturn(true);

        assertThrows(ConflictException.class, () -> reservationTimeService.save(LocalTime.parse("10:00")));
    }

    @Test
    @DisplayName("특정 날짜와 테마에 이미 예약된 시간을 제외한 예약 가능 시간을 조회한다")
    void findAvailableTimes() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );
        ReservationTime ten = ReservationTime.of(1L, LocalTime.parse("10:00"));
        ReservationTime eleven = ReservationTime.of(2L, LocalTime.parse("11:00"));
        LocalDate date = LocalDate.parse("2026-08-06");

        when(themeService.getById(1L)).thenReturn(Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png"));
        when(reservationRepository.findReservedTimeIdsByDateAndThemeId(date, 1L)).thenReturn(List.of(1L));
        when(reservationTimeRepository.findAll()).thenReturn(List.of(ten, eleven));

        List<ReservationTime> availableTimes = reservationTimeService.findAvailableTimes(date, 1L);

        assertThat(availableTimes)
                .extracting(ReservationTime::getId)
                .containsExactly(eleven.getId());
    }

    @Test
    @DisplayName("지난 날짜에 대해서는 예약 가능 시간이 조회되지 않는다")
    void findAvailableTimesInPastDate() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );
        LocalDate pastDate = LocalDate.now().minusDays(1);

        when(themeService.getById(1L)).thenReturn(Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png"));
        when(reservationRepository.findReservedTimeIdsByDateAndThemeId(pastDate, 1L)).thenReturn(List.of());
        when(reservationTimeRepository.findAll()).thenReturn(List.of(
                ReservationTime.of(1L, LocalTime.parse("10:00")),
                ReservationTime.of(2L, LocalTime.parse("11:00"))
        ));

        List<ReservationTime> availableTimes = reservationTimeService.findAvailableTimes(pastDate, 1L);

        assertThat(availableTimes).isEmpty();
    }

    @Test
    @DisplayName("오늘 날짜 조회 시 이미 지난 시간은 예약 가능 시간에서 제외된다")
    void findAvailableTimesTodayExcludesPastTimes() {
        assumeTrue(LocalTime.now().isBefore(LocalTime.of(23, 59)));

        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        LocalTime futureTime = now.plusMinutes(1);
        ReservationTime past = ReservationTime.of(1L, pastTime);
        ReservationTime future = ReservationTime.of(2L, futureTime);

        when(themeService.getById(1L)).thenReturn(Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png"));
        when(reservationRepository.findReservedTimeIdsByDateAndThemeId(LocalDate.now(), 1L)).thenReturn(List.of());
        when(reservationTimeRepository.findAll()).thenReturn(List.of(past, future));

        List<ReservationTime> availableTimes = reservationTimeService.findAvailableTimes(LocalDate.now(), 1L);

        assertThat(availableTimes)
                .extracting(ReservationTime::getId)
                .contains(future.getId())
                .doesNotContain(past.getId());
    }

    @Test
    @DisplayName("예약이 존재하는 예약 시간은 삭제할 수 없다")
    void deleteById() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );

        when(reservationRepository.existsByTimeId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> reservationTimeService.deleteById(1L));
    }

    @Test
    @DisplayName("예약이 없는 예약 시간을 삭제한다")
    void deleteByIdWithoutReservation() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );

        when(reservationRepository.existsByTimeId(1L)).thenReturn(false);
        when(reservationTimeRepository.deleteById(1L)).thenReturn(1);

        reservationTimeService.deleteById(1L);

        verify(reservationTimeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간은 삭제할 수 없다")
    void deleteByIdNotFound() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );

        when(reservationRepository.existsByTimeId(1L)).thenReturn(false);
        when(reservationTimeRepository.deleteById(1L)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> reservationTimeService.deleteById(1L));
    }

    @Test
    @DisplayName("ID로 예약 시간을 조회한다")
    void getById() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.parse("10:00"));

        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(reservationTime));

        ReservationTime found = reservationTimeService.getById(1L);

        assertThat(found).isEqualTo(reservationTime);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 예약 시간을 조회할 수 없다")
    void getByIdNotFound() {
        JpaReservationTimeRepository reservationTimeRepository = mock(JpaReservationTimeRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );

        when(reservationTimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservationTimeService.getById(1L));
    }
}
