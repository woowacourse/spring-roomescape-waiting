package roomescape.theme.domain.fake;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new CopyOnWriteArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Theme save(Theme theme) {
        Theme saved = Theme.restore(idGenerator.getAndIncrement(), theme.getName(), theme.getThumbnailImageUrl(),
                theme.getDescription(), theme.isActive());
        themes.add(saved);
        return saved;
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themes.stream().filter(theme -> theme.getId().equals(id)).findFirst();
    }

    @Override
    public List<Theme> findAll(int page, int size) {
        return themes.stream()
                .filter(Theme::isActive)
                .sorted(Comparator.comparing(Theme::getId))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public List<Theme> findByReservationCountWithLimit(LocalDate startDate, LocalDate endDate, int limit) {
        return themes.stream()
                .filter(Theme::isActive)
                .sorted(Comparator.comparing(Theme::getId))
                .limit(limit)
                .toList();
    }

    @Override
    public boolean existsByName(String name) {
        return themes.stream().anyMatch(theme -> theme.getName().equals(name) && theme.isActive());
    }

    @Override
    public void update(Theme theme) {
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).getId().equals(theme.getId())) {
                themes.set(i, theme);
                return;
            }
        }
    }
}
