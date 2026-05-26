package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.reservation.MemoryReservationRepository;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.theme.ThemeRepository;
import roomescape.service.theme.ThemeService;

class ThemeServiceTest {

    @Test
    @DisplayName("테마를 저장한다")
    void save() {
        ThemeService themeService = new ThemeService(new TestThemeRepository(), new MemoryReservationRepository());

        Theme saved = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("미술관의 밤");
    }

    @Test
    @DisplayName("중복된 이름의 테마는 저장할 수 없다")
    void saveDuplicateName() {
        ThemeService themeService = new ThemeService(new TestThemeRepository(), new MemoryReservationRepository());
        themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");

        assertThrows(
                ConflictException.class,
                () -> themeService.save("미술관의 밤", "새 설명", "https://example.com/new-theme.png")
        );
    }

    @Test
    @DisplayName("ID로 테마를 조회한다")
    void getById() {
        ThemeService themeService = new ThemeService(new TestThemeRepository(), new MemoryReservationRepository());
        Theme saved = themeService.save("미술관의 밤", "추리 테마", "https://example.com/theme.png");

        Theme found = themeService.getById(saved.getId());

        assertThat(found).isEqualTo(saved);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 테마를 조회할 수 없다")
    void getByIdNotFound() {
        ThemeService themeService = new ThemeService(new TestThemeRepository(), new MemoryReservationRepository());

        assertThrows(ResourceNotFoundException.class, () -> themeService.getById(1L));
    }

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제할 수 없다")
    void deleteById() {
        TestThemeRepository themeRepository = new TestThemeRepository();
        MemoryReservationRepository reservationRepository = new MemoryReservationRepository();
        ThemeService themeService = new ThemeService(themeRepository, reservationRepository);

        Theme theme = themeRepository.save(Theme.createNew("미술관의 밤", "추리 테마", "https://example.com/theme.png"));
        reservationRepository.save(Reservation.of(
                1L,
                "쿠다",
                java.time.LocalDate.parse("2026-08-06"),
                theme,
                roomescape.domain.reservationtime.ReservationTime.of(1L, java.time.LocalTime.parse("10:00"))
        ));

        assertThrows(ConflictException.class, () -> themeService.deleteById(theme.getId()));
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
