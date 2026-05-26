package roomescape.service.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme>  themes = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Theme save(Theme theme) {
        long id = idGenerator.getAndIncrement();
        Theme savedTheme = new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl(),
                theme.isActive()
        );
        themes.put(id, savedTheme);
        return savedTheme;
    }

    @Override
    public void update(Theme theme) {
        themes.put(theme.getId(), theme);
    }

    @Override
    public Optional<Theme> findById(long id) {
        return Optional.ofNullable(themes.get(id));
    }

    @Override
    public List<Theme> findAllActiveThemes() {
        return themes.values().stream().filter(Theme::isActive).toList();
    }

    @Override
    public List<Theme> findTop10ByReservationCount(LocalDate startDate, LocalDate endDate) {
        return findAllActiveThemes().subList(0, 10);
    }

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(themes.values());
    }

    @Override
    public boolean isActiveByName(String name) {
        return themes.values().stream()
                .anyMatch(theme -> theme.getName().equals(name) && theme.isActive());
    }
}
