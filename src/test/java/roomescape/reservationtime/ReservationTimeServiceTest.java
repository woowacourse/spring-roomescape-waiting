package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationScheduleRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;
import roomescape.support.FakeReservationRepository;
import roomescape.support.FakeReservationTimeRepository;
import roomescape.support.FakeThemeRepository;

class ReservationTimeServiceTest {

    @Test
    @DisplayName("예약 시간을 저장한다")
    void save() {
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );
        ReservationTime savedTime = ReservationTime.of(1L, LocalTime.parse("10:00"));

        ReservationTime saved = fixture.reservationTimeService.save(LocalTime.parse("10:00"));

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(fixture.reservationTimeRepository.findById(saved.getId())).contains(saved);
    }

    @Test
    @DisplayName("중복된 예약 시간은 저장할 수 없다")
    void saveDuplicateTime() {
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );

        assertThrows(ConflictException.class, () -> fixture.reservationTimeService.save(LocalTime.parse("10:00")));
    }

    @Test
    @DisplayName("특정 날짜와 테마에 이미 예약된 시간을 제외한 예약 가능 시간을 조회한다")
    void findAvailableTimes() {
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );
        ReservationTime ten = ReservationTime.of(1L, LocalTime.parse("10:00"));
        ReservationTime eleven = ReservationTime.of(2L, LocalTime.parse("11:00"));
        LocalDate date = LocalDate.parse("2026-08-06");
        fixture.reservationRepository.save(Reservation.createNew("쿠다", date, theme, ten));

        when(themeService.getById(1L)).thenReturn(Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png"));
        when(reservationScheduleRepository.findReservedTimeIdsByDateAndThemeId(date, 1L)).thenReturn(List.of(1L));
        when(reservationTimeRepository.findAll()).thenReturn(List.of(ten, eleven));

        List<ReservationTime> availableTimes = reservationTimeService.findAvailableTimes(date, 1L);

        assertThat(availableTimes)
                .extracting(ReservationTime::getId)
                .containsExactly(eleven.getId());
    }

    @Test
    @DisplayName("지난 날짜에 대해서는 예약 가능 시간이 조회되지 않는다")
    void findAvailableTimesInPastDate() {
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );
        LocalDate pastDate = LocalDate.now().minusDays(1);
        fixture.reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        fixture.reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));

        when(themeService.getById(1L)).thenReturn(Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png"));
        when(reservationScheduleRepository.findReservedTimeIdsByDateAndThemeId(pastDate, 1L)).thenReturn(List.of());
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

        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        LocalTime futureTime = now.plusMinutes(1);
        ReservationTime past = fixture.reservationTimeRepository.save(ReservationTime.createNew(pastTime));
        ReservationTime future = fixture.reservationTimeRepository.save(ReservationTime.createNew(futureTime));

        when(themeService.getById(1L)).thenReturn(Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png"));
        when(reservationScheduleRepository.findReservedTimeIdsByDateAndThemeId(LocalDate.now(), 1L)).thenReturn(List.of());
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
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );

        when(reservationScheduleRepository.existsByTimeId(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> reservationTimeService.deleteById(1L));
    }

    @Test
    @DisplayName("예약이 없는 예약 시간을 삭제한다")
    void deleteByIdWithoutReservation() {
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );

        when(reservationScheduleRepository.existsByTimeId(1L)).thenReturn(false);
        when(reservationTimeRepository.deleteById(1L)).thenReturn(1);

        assertThat(fixture.reservationTimeRepository.findById(time.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간은 삭제할 수 없다")
    void deleteByIdNotFound() {
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );

        when(reservationScheduleRepository.existsByTimeId(1L)).thenReturn(false);
        when(reservationTimeRepository.deleteById(1L)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> reservationTimeService.deleteById(1L));
    }

    @Test
    @DisplayName("ID로 예약 시간을 조회한다")
    void getById() {
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );

        ReservationTime found = fixture.reservationTimeService.getById(reservationTime.getId());

        assertThat(found).isEqualTo(reservationTime);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 예약 시간을 조회할 수 없다")
    void getByIdNotFound() {
        ReservationTimeRepository reservationTimeRepository = mock(ReservationTimeRepository.class);
        ReservationScheduleRepository reservationScheduleRepository = mock(ReservationScheduleRepository.class);
        ThemeService themeService = mock(ThemeService.class);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationScheduleRepository,
                themeService
        );

        private Theme saveTheme() {
            return themeRepository.save(
                    Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png")
            );
        }
    }
}
