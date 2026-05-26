package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.reservation.MemoryReservationRepository;
import roomescape.service.reservation.ReservationService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.exception.ConflictException;
import roomescape.exception.InvalidInputException;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.service.reservation.ReservationValidator;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.repository.theme.ThemeRepository;
import roomescape.service.theme.ThemeService;

class ReservationServiceTest {

    @Test
    @DisplayName("예약을 저장한다")
    void save() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = fixture.reservationTimeService.save(LocalTime.parse("10:00"));

        Reservation saved = fixture.reservationService.save("쿠다", LocalDate.parse("2026-08-06"), theme.getId(), time.getId());

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getTheme().getId()).isEqualTo(theme.getId());
        assertThat(saved.getTime().getId()).isEqualTo(time.getId());
    }

    @Test
    @DisplayName("같은 날짜, 테마, 시간 조합의 중복 예약은 저장할 수 없다")
    void saveDuplicateReservation() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = fixture.reservationTimeService.save(LocalTime.parse("10:00"));
        LocalDate date = LocalDate.parse("2026-08-06");
        fixture.reservationService.save("쿠다", date, theme.getId(), time.getId());

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.save("아루", date, theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("과거 날짜로는 예약할 수 없다")
    void savePastDate() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = fixture.reservationTimeService.save(LocalTime.parse("10:00"));

        assertThrows(
                InvalidInputException.class,
                () -> fixture.reservationService.save("쿠다", LocalDate.now().minusDays(1), theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("오늘 날짜라도 이미 지난 시간으로는 예약할 수 없다")
    void savePastTimeToday() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime time = fixture.reservationTimeService.save(pastTime);

        assertThrows(
                InvalidInputException.class,
                () -> fixture.reservationService.save("쿠다", LocalDate.now(), theme.getId(), time.getId())
        );
    }

    @Test
    @DisplayName("예약을 삭제한다")
    void deleteById() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime time = fixture.reservationTimeService.save(LocalTime.parse("10:00"));
        Reservation saved = fixture.reservationService.save("쿠다", LocalDate.parse("2026-08-06"), theme.getId(), time.getId());

        fixture.reservationService.deleteById(saved.getId());

        assertThat(fixture.reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("지난 예약은 취소할 수 없다")
    void deleteByIdAndNamePastReservation() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime time = fixture.reservationTimeService.save(pastTime);
        Reservation saved = fixture.reservationRepository.save(
                Reservation.createNew("쿠다", LocalDate.now(), theme, time)
        );

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.deleteByIdAndName(saved.getId(), "쿠다")
        );
    }

    @Test
    @DisplayName("조회한 이름의 예약 날짜와 시간을 변경한다")
    void updateByIdAndName() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime firstTime = fixture.reservationTimeService.save(LocalTime.parse("10:00"));
        ReservationTime secondTime = fixture.reservationTimeService.save(LocalTime.parse("11:00"));
        Reservation saved = fixture.reservationService.save("쿠다", LocalDate.parse("2026-08-06"), theme.getId(), firstTime.getId());

        Reservation updated = fixture.reservationService.updateByIdAndName(
                saved.getId(),
                "쿠다",
                LocalDate.parse("2026-08-07"),
                secondTime.getId()
        );

        assertThat(updated.getDate()).isEqualTo(LocalDate.parse("2026-08-07"));
        assertThat(updated.getTime().getId()).isEqualTo(secondTime.getId());
    }

    @Test
    @DisplayName("조회한 이름의 다른 예약과 시간이 겹치면 변경할 수 없다")
    void updateByIdAndNameDuplicate() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime firstTime = fixture.reservationTimeService.save(LocalTime.parse("10:00"));
        ReservationTime secondTime = fixture.reservationTimeService.save(LocalTime.parse("11:00"));
        LocalDate date = LocalDate.parse("2026-08-06");
        Reservation target = fixture.reservationService.save("쿠다", date, theme.getId(), firstTime.getId());
        fixture.reservationService.save("쿠다", date, theme.getId(), secondTime.getId());

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(target.getId(), "쿠다", date, secondTime.getId())
        );
    }

    @Test
    @DisplayName("이미 지난 예약은 변경할 수 없다")
    void updateByIdAndNamePastReservation() {
        Fixture fixture = new Fixture();
        Theme theme = fixture.themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime pastTime = now.equals(LocalTime.MIDNIGHT) ? now : now.minusMinutes(1);
        ReservationTime savedTime = fixture.reservationTimeService.save(pastTime);
        Reservation saved = fixture.reservationRepository.save(
                Reservation.createNew("쿠다", LocalDate.now(), theme, savedTime)
        );
        ReservationTime futureTime = fixture.reservationTimeService.save(now.plusMinutes(10));

        assertThrows(
                ConflictException.class,
                () -> fixture.reservationService.updateByIdAndName(
                        saved.getId(),
                        "쿠다",
                        LocalDate.now().plusDays(1),
                        futureTime.getId()
                )
        );
    }

    private static class Fixture {
        private final MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        private final ThemeService themeService =
                new ThemeService(new TestThemeRepository(), reservationRepository);
        private final ReservationTimeService reservationTimeService =
                new ReservationTimeService(new TestReservationTimeRepository(), reservationRepository, themeService);
        private final ReservationValidator reservationValidator = new ReservationValidator();
        private final ReservationService reservationService =
                new ReservationService(reservationRepository, reservationTimeService, themeService, reservationValidator);
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
}
