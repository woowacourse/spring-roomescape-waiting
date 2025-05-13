package roomescape.testRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.NotFoundException;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new ArrayList<>();
    
    private Long index = 0L;

    @Override
    public Long save(Theme theme) {
        Theme themeWithId = Theme.assignId(++index, theme);
        themes.add(themeWithId);
        return index;
    }

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themes.stream()
                .filter(theme -> theme.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean deleteById(Long id) {
        Theme theme = findById(id)
                .orElseThrow(() -> new NotFoundException("FakeTimeRepository: 삭제하려는 id 없음"));
        themes.remove(theme);
        return true;
    }

    @Override
    public List<Theme> findThemeRanking(int count, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }
}
