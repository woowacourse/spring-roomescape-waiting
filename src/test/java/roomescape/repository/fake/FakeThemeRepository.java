package roomescape.repository.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.repository.ThemeRepository;
import roomescape.entity.Theme;
import roomescape.exception.custom.NotFoundException;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new ArrayList<>();

    public List<Theme> findAll() {
        return Collections.unmodifiableList(themes);
    }

    public Optional<Theme> findById(Long id) {
        return themes.stream()
            .filter(t -> t.getId().equals(id))
            .findFirst();
    }

    public List<Theme> findTop10ByDateBetween(LocalDate startDate, LocalDate endDate) {
        return themes.stream()
            .limit(10)
            .toList();
    }

    public boolean existsByName(String name) {
        return themes.stream()
            .anyMatch(t -> t.getName().equals(name));
    }

    public Theme save(Theme theme) {
        Theme newTheme = new Theme(
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnail());

        themes.add(newTheme);
        return newTheme;
    }

    public void deleteById(Long id) {
        themes.removeIf(t -> t.getId().equals(id));
    }
}
