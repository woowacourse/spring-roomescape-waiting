package roomescape.theme.fixture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.repository.projection.PopularThemeResult;

public class FakeThemeRepository implements ThemeRepository {
    private final List<Theme> themes = new ArrayList<>();
    private Long autoIncrement = 0L;

    @Override
    public Theme save(Theme theme) {
        autoIncrement();
        Theme savedTheme = Theme.load(autoIncrement, theme.getName(), theme.getDescription(), theme.getThumbnailUrl(), theme.isActive(), theme.getAmount());
        themes.add(savedTheme);
        return savedTheme;
    }

    public List<Theme> saveAll(List<Theme> themes) {
        List<Theme> savedThemes = new ArrayList<>();
        for (Theme theme : themes) {
            Theme savedTheme = save(theme);
            savedThemes.add(savedTheme);
        }
        return savedThemes;
    }

    private void autoIncrement() {
        autoIncrement = autoIncrement + 1;
    }

    @Override
    public boolean updateStatus(Theme theme) {
        boolean isActive = theme.isActive();
        Optional<Theme> target = findById(theme.getId());
        if (target.isEmpty()) {
            return false;
        }
        target.get().updateStatus(isActive);
        return true;
    }

    @Override
    public List<Theme> findAll() {
        return themes.stream().toList();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themes.stream()
                .filter(theme -> theme.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Theme> findByIsActive(boolean isActive) {
        return themes.stream()
                .filter(theme -> theme.isActive() == isActive)
                .sorted(Comparator.comparing(Theme::getName))
                .toList();
    }

    @Override
    public List<PopularThemeResult> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        return List.of();
    }

    @Override
    public List<Theme> findSlotOfThemes() {
        return List.of();
    }

}
