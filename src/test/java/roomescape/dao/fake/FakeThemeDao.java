package roomescape.dao.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.ThemeDao;
import roomescape.entity.Theme;
import roomescape.exception.custom.NotFoundException;

public class FakeThemeDao implements ThemeDao {

    private final List<Theme> themes = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    public List<Theme> findAllThemes() {
        return Collections.unmodifiableList(themes);
    }

    public Theme findThemeById(Long id) {
        return themes.stream()
            .filter(t -> t.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("theme"));
    }

    public List<Theme> findTopReservedThemesInPeriodWithLimit(LocalDate startDate,
        LocalDate endDate, int limitCount) {
        return themes.stream()
            .limit(limitCount)
            .toList();
    }

    public boolean existThemeByName(String name) {
        return themes.stream()
            .anyMatch(t -> t.getName().equals(name));
    }

    public Theme addTheme(Theme theme) {
        Theme newTheme = new Theme(
            index.getAndIncrement(),
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnail());

        themes.add(newTheme);
        return newTheme;
    }

    public void removeThemeById(Long id) {
        themes.removeIf(t -> t.getId().equals(id));
    }
}
