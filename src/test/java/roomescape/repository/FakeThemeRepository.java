package roomescape.repository;

import static roomescape.repository.FakeReservationRepository.RESERVATION_TABLE;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public class FakeThemeRepository implements ThemeRepository {

    public static final String THEME_TABLE = "theme";

    private final FakeDatabase fakeDatabase;
    private Long currentId = 0L;

    public FakeThemeRepository(FakeDatabase fakeDatabase) {
        this.fakeDatabase = fakeDatabase;
    }

    @Override
    public Theme create(Theme themeWithoutId) {
        Theme theme = Theme.of(++currentId, themeWithoutId);
        fakeDatabase.create(THEME_TABLE, theme.getId(), theme);

        return theme;
    }

    @Override
    public Optional<Theme> read(Long id) {
        return Optional.ofNullable(fakeDatabase.read(THEME_TABLE, id, Theme.class));
    }

    @Override
    public List<Theme> readAll() {
        return fakeDatabase.readAll(THEME_TABLE, Theme.class);
    }

    @Override
    public void delete(Long id) {
        fakeDatabase.delete(THEME_TABLE, id);
    }

    @Override
    public List<Theme> readRanking(LocalDate startDate, LocalDate endDate, int limit) {
        List<Reservation> reservations = fakeDatabase.readAll(RESERVATION_TABLE, Reservation.class);

        Map<Theme, Long> reservationCount = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getTheme, Collectors.counting()));

        List<Theme> ranking = reservationCount.entrySet().stream()
                .sorted(Entry.comparingByValue())
                .map(Entry::getKey)
                .toList()
                .reversed();

        if (ranking.size() < limit) {
            return ranking;
        }
        return ranking.subList(0, limit);
    }

    @Override
    public boolean existById(Long id) {
        return fakeDatabase.readAll(THEME_TABLE, Theme.class).stream()
                .anyMatch(theme -> theme.getId().equals(id));
    }
}
