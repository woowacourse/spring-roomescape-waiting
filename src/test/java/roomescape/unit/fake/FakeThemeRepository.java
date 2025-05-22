package roomescape.unit.fake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> fakeThemes = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    public FakeThemeRepository(Theme... themes) {
        Arrays.stream(themes)
                .forEach(theme -> fakeThemes.add(theme));
    }

    @Override
    public Theme save(Theme theme) {
        Theme themeWithId = new Theme(index.getAndIncrement(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
        fakeThemes.add(themeWithId);
        return themeWithId;
    }

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(fakeThemes);
    }

    @Override
    public void deleteById(long id) {
        fakeThemes.removeIf(theme -> theme.getId().equals(id));
    }

    @Override
    public Optional<Theme> findByName(String name) {
        return fakeThemes.stream()
                .filter(theme -> theme.getName().equals(name))
                .findFirst();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return fakeThemes.stream().filter(theme -> theme.getId().equals(id)).findFirst();
    }
}
