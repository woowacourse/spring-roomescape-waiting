package roomescape.repository.fake;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.PopularTheme;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> store = new HashMap<>();
    private long nextId = 1L;
    private final FakeReservationRepository reservationRepository;

    public FakeThemeRepository(FakeReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Theme> findAll(int limit, int offset) {
        return store.values().stream()
                .sorted(Comparator.comparing(Theme::getId))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Long save(Theme theme) {
        Long id = nextId++;
        store.put(id, theme.withId(id));
        return id;
    }

    @Override
    public int deleteById(Long id) {
        return store.remove(id) == null ? 0 : 1;
    }

    @Override
    public List<PopularTheme> findPopularThemes(LocalDate startDate, LocalDate endDate, Integer limit) {
        return store.values().stream()
                .map(theme -> new PopularTheme(theme, countInRange(theme.getId(), startDate, endDate)))
                .filter(pt -> pt.getReservationCount() > 0)
                .sorted(Comparator.<PopularTheme>comparingLong(PopularTheme::getReservationCount).reversed()
                        .thenComparing(pt -> pt.getTheme().getId()))
                .limit(limit)
                .toList();
    }

    private long countInRange(Long themeId, LocalDate start, LocalDate end) {
        return reservationRepository.all().stream()
                .filter(r -> r.getTheme().getId().equals(themeId))
                .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .count();
    }
}