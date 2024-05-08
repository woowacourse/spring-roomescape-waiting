package roomescape.service.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import roomescape.model.Theme;
import roomescape.repository.ThemeDao;

public class FakeThemeDao implements ThemeDao {

    private final List<Theme> themes = new ArrayList<>();

    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public List<Theme> findAllThemes() {
        return themes;
    }

    @Override
    public Theme addTheme(Theme theme) {
        Theme newTheme = new Theme(index.getAndIncrement(), theme.getName(), theme.getDescription(),
                theme.getThumbnail());
        themes.add(newTheme);
        return newTheme;
    }

    @Override
    public void deleteTheme(long id) {
        Theme targetTheme = themes.stream()
                .filter(theme -> theme.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 테마입니다."));
        themes.remove(targetTheme);
    }

    @Override
    public Theme findThemeById(long id) {
        return themes.stream()
                .filter(theme -> theme.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 테마입니다."));
    }

    @Override
    public List<Theme> findThemeRankingByDate(LocalDate before, LocalDate after, int limit) {
        return List.of(
                new Theme(10L, "name10", "description10", "thumbnail10"),
                new Theme(9L, "name9", "description9", "thumbnail9"),
                new Theme(1L, "name1", "description1", "thumbnail1"),
                new Theme(2L, "name2", "description2", "thumbnail2"),
                new Theme(3L, "name3", "description3", "thumbnail3"),
                new Theme(4L, "name4", "description4", "thumbnail4"),
                new Theme(5L, "name5", "description5", "thumbnail5"),
                new Theme(6L, "name6", "description6", "thumbnail6"),
                new Theme(7L, "name7", "description7", "thumbnail7"),
                new Theme(8L, "name8", "description8", "thumbnail8")
        );
    }

    public void clear() {
        index.set(1L);
        themes.clear();
    }
}
