package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.reservation.MemoryReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.repository.theme.ThemeRepository;
import roomescape.service.theme.ThemeService;

class ReservationTimeServiceTest {

    @Test
    @DisplayName("예약 시간을 저장한다")
    void save() {
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                new TestReservationTimeRepository(),
                reservationRepository,
                createThemeService(reservationRepository)
        );

        ReservationTime saved = reservationTimeService.save(LocalTime.parse("10:00"));

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.parse("10:00"));
    }

    @Test
    @DisplayName("중복된 예약 시간은 저장할 수 없다")
    void saveDuplicateTime() {
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                new TestReservationTimeRepository(),
                reservationRepository,
                createThemeService(reservationRepository)
        );
        reservationTimeService.save(LocalTime.parse("10:00"));

        assertThrows(ConflictException.class, () -> reservationTimeService.save(LocalTime.parse("10:00")));
    }

    @Test
    @DisplayName("특정 날짜와 테마에 이미 예약된 시간을 제외한 예약 가능 시간을 조회한다")
    void findAvailableTimes() {
        ReservationTimeRepository reservationTimeRepository = new TestReservationTimeRepository();
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ThemeService themeService = createThemeService(reservationRepository);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );

        Theme firstTheme = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        Theme secondTheme = themeService.save("심해 연구소", "SF 테마", "https://example.com/theme2.png");
        ReservationTime ten = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        ReservationTime eleven = reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));
        LocalDate date = LocalDate.parse("2026-08-06");

        reservationRepository.save(Reservation.createNew("쿠다", date, firstTheme, ten));
        reservationRepository.save(Reservation.createNew("포비", date, secondTheme, ten));

        List<ReservationTime> availableTimes = reservationTimeService.findAvailableTimes(date, firstTheme.getId());

        assertThat(availableTimes)
                .extracting(ReservationTime::getId)
                .containsExactly(eleven.getId());
    }

    @Test
    @DisplayName("지난 날짜에 대해서는 예약 가능 시간이 조회되지 않는다")
    void findAvailableTimesInPastDate() {
        ReservationTimeRepository reservationTimeRepository = new TestReservationTimeRepository();
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ThemeService themeService = createThemeService(reservationRepository);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );
        Theme theme = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");

        reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("10:00")));
        reservationTimeRepository.save(ReservationTime.createNew(LocalTime.parse("11:00")));

        List<ReservationTime> availableTimes = reservationTimeService.findAvailableTimes(
                LocalDate.now().minusDays(1),
                theme.getId()
        );

        assertThat(availableTimes).isEmpty();
    }

    @Test
    @DisplayName("오늘 날짜 조회 시 이미 지난 시간은 예약 가능 시간에서 제외된다")
    void findAvailableTimesTodayExcludesPastTimes() {
        assumeTrue(LocalTime.now().isBefore(LocalTime.of(23, 59)));

        ReservationTimeRepository reservationTimeRepository = new TestReservationTimeRepository();
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ThemeService themeService = createThemeService(reservationRepository);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );
        Theme theme = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");

        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        LocalTime futureTime = now.plusMinutes(1);

        ReservationTime past = reservationTimeRepository.save(ReservationTime.createNew(pastTime));
        ReservationTime future = reservationTimeRepository.save(ReservationTime.createNew(futureTime));

        List<ReservationTime> availableTimes = reservationTimeService.findAvailableTimes(
                LocalDate.now(),
                theme.getId()
        );

        assertThat(availableTimes)
                .extracting(ReservationTime::getId)
                .contains(future.getId())
                .doesNotContain(past.getId());
    }

    @Test
    @DisplayName("예약이 존재하는 예약 시간은 삭제할 수 없다")
    void deleteById() {
        ReservationTimeRepository reservationTimeRepository = new TestReservationTimeRepository();
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ThemeService themeService = createThemeService(reservationRepository);
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                themeService
        );

        ReservationTime savedTime = reservationTimeService.save(LocalTime.parse("10:00"));
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        reservationRepository.save(Reservation.createNew("쿠다", LocalDate.parse("2026-08-06"), theme, savedTime));

        assertThrows(ConflictException.class, () -> reservationTimeService.deleteById(savedTime.getId()));
    }

    @Test
    @DisplayName("ID로 예약 시간을 조회한다")
    void getById() {
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                new TestReservationTimeRepository(),
                reservationRepository,
                createThemeService(reservationRepository)
        );
        ReservationTime saved = reservationTimeService.save(LocalTime.parse("10:00"));

        ReservationTime found = reservationTimeService.getById(saved.getId());

        assertThat(found).isEqualTo(saved);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 예약 시간을 조회할 수 없다")
    void getByIdNotFound() {
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ReservationTimeService reservationTimeService = new ReservationTimeService(
                new TestReservationTimeRepository(),
                reservationRepository,
                createThemeService(reservationRepository)
        );

        assertThrows(ResourceNotFoundException.class, () -> reservationTimeService.getById(1L));
    }

    private static class TestReservationTimeRepository implements ReservationTimeRepository {

        private final List<ReservationTime> reservationTimes = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public List<ReservationTime> findAll() {
            return List.copyOf(reservationTimes);
        }

        @Override
        public Optional<ReservationTime> findById(final long timeId) {
            return reservationTimes.stream()
                    .filter(reservationTime -> reservationTime.getId() == timeId)
                    .findFirst();
        }

        @Override
        public void deleteById(final long timeId) {
            reservationTimes.removeIf(reservationTime -> reservationTime.getId() == timeId);
        }

        @Override
        public ReservationTime save(final ReservationTime reservationTime) {
            ReservationTime saved = reservationTime.withId(nextId++);
            reservationTimes.add(saved);
            return saved;
        }

        @Override
        public boolean existsByStartAt(final LocalTime startAt) {
            return reservationTimes.stream()
                    .anyMatch(reservationTime -> reservationTime.getStartAt().equals(startAt));
        }
    }

    private ThemeService createThemeService(final MemoryReservationRepository reservationRepository) {
        return new ThemeService(new TestThemeRepository(), reservationRepository);
    }

    private static class TestThemeRepository implements ThemeRepository {

        private final List<Theme> themes = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public List<Theme> findAll() {
            return List.copyOf(themes);
        }

        @Override
        public Optional<Theme> findById(final long id) {
            return themes.stream()
                    .filter(theme -> theme.getId() == id)
                    .findFirst();
        }

        @Override
        public void deleteById(final long id) {
            themes.removeIf(theme -> theme.getId() == id);
        }

        @Override
        public Theme save(final Theme theme) {
            Theme saved = theme.withId(nextId++);
            themes.add(saved);
            return saved;
        }

        @Override
        public boolean existsByName(final String name) {
            return themes.stream().anyMatch(theme -> theme.getName().equals(name));
        }

        @Override
        public List<Theme> findPopularThemes(final int period, final int limit) {
            throw new UnsupportedOperationException();
        }
    }
}
