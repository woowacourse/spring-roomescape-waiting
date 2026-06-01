package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.PopularTheme;
import roomescape.theme.domain.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> themes = new LinkedHashMap<>();
    private final List<PopularTheme> popularThemes = new ArrayList<>();
    private Long idHolder = 1L;
    @Getter
    private LocalDate from;
    @Getter
    private LocalDate to;

    @Override
    public Optional<Theme> findById(Long id) {
        return Optional.ofNullable(themes.get(id));
    }

    @Override
    public List<Theme> findAll() {
        return themes.values().stream()
                .toList();
    }

    @Override
    public List<PopularTheme> findTop10PopularThemesBetween(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
        return popularThemes;
    }

    @Override
    public Theme save(Theme theme) {
        Theme savedTheme = theme.withId(idHolder);
        themes.put(idHolder++, savedTheme);
        return savedTheme;
    }

    @Override
    public Integer delete(long id) {
        int beforeSize = themes.size();
        themes.remove(id);
        int afterSize = themes.size();

        if (beforeSize != afterSize) {
            return 1;
        }

        return 0;
    }

    @Override
    public Boolean existsByNameAndDescription(Theme theme) {
        return themes.values().stream()
                .anyMatch(savedTheme -> savedTheme.equals(theme));
    }

    public void savePopularTheme(PopularTheme popularTheme) {
        popularThemes.add(popularTheme);
    }
}
