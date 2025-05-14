package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ThemeDao;
import roomescape.model.Theme;

public class FakeThemeDao implements ThemeDao {

    private final List<Theme> themes = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(themes);
    }

    @Override
    public Long saveTheme(Theme theme) {
        Theme newTheme = new Theme(nextId.getAndIncrement(), theme.getName(), theme.getDescription(),
                theme.getThumbnail());
        themes.add(newTheme);
        return newTheme.getId();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themes.stream()
                .filter(theme -> theme.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean isDuplicatedNameExisted(String name) {
        return themes.stream()
                .anyMatch(theme -> theme.getName().equals(name));
    }

    @Override
    public List<Theme> getTopReservedThemesSince(LocalDate today, int dayRange, int size) {
        return List.of();
    }

    @Override
    public void deleteById(Long id) {
        Optional<Theme> foundTheme = themes.stream()
                .filter(theme -> theme.getId().equals(id))
                .findAny();
        foundTheme.ifPresent(themes::remove);
    }
}
