package roomescape.theme.service.support;

import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeInUseException;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new ArrayList<>();
    private Theme savedTheme;
    private List<Theme> popularThemes = List.of();
    private LocalDate popularStartDate;
    private LocalDate popularToday;
    private boolean deleteResult = true;
    private RuntimeException deleteException;

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public Optional<Theme> findById(final Long themeId) {
        return themes.stream()
                .filter(theme -> theme.getId().equals(themeId))
                .findFirst();
    }

    @Override
    public Theme save(final Theme themeWithoutId) {
        savedTheme = themeWithoutId;
        Theme savedThemeWithId = Theme.of(
                1L,
                themeWithoutId.getName(),
                themeWithoutId.getDescription(),
                themeWithoutId.getThumbnailUrl()
        );
        themes.add(savedThemeWithId);
        return savedThemeWithId;
    }

    @Override
    public boolean deleteById(final Long themeId) {
        if (deleteException != null) {
            throw deleteException;
        }

        return deleteResult;
    }

    @Override
    public List<Theme> findPopularThemes(final LocalDate startDate, final LocalDate today) {
        popularStartDate = startDate;
        popularToday = today;
        return popularThemes;
    }

    public void add(final Theme theme) {
        themes.add(theme);
    }

    public Theme savedTheme() {
        return savedTheme;
    }

    public void setPopularThemes(final List<Theme> popularThemes) {
        this.popularThemes = popularThemes;
    }

    public LocalDate popularStartDate() {
        return popularStartDate;
    }

    public LocalDate popularToday() {
        return popularToday;
    }

    public void failToDelete() {
        deleteResult = false;
    }

    public void failToDeleteByInUse() {
        deleteException = new ThemeInUseException(new RuntimeException("theme in use"));
    }
}
