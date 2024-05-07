package roomescape.service.fakedao;

import roomescape.model.theme.Theme;
import roomescape.repository.dao.ThemeDao;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeThemeDao implements ThemeDao {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<Theme> themes = new ArrayList<>();

    public FakeThemeDao(List<Theme> themes) {
        themes.forEach(this::save);
    }

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public long save(Theme theme) {
        long key = index.getAndIncrement();
        themes.add(new Theme(key, theme.getName(), theme.getDescription(), theme.getThumbnail()));
        return key;
    }

    @Override
    public void deleteById(long id) {
        Theme targetTheme = themes.stream()
                .filter(theme -> theme.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 테마입니다."));
        themes.remove(targetTheme);
    }

    @Override
    public Boolean isExistById(long id) {
        return themes.stream().anyMatch(theme -> theme.getId() == id);
    }

    @Override
    public Boolean isExistByName(String name) {
        return themes.stream().anyMatch(theme -> theme.getName().equals(name));
    }

    @Override
    public Optional<Theme> findById(long id) {
        return themes.stream()
                .filter(theme -> theme.getId() == id)
                .findFirst();
    }
}
