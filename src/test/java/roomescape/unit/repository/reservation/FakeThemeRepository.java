package roomescape.unit.repository.reservation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.theme.Theme;
import roomescape.repository.theme.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<Theme> themes = new ArrayList<>();

    @Override
    public long save(Theme theme) {
        long id = index.getAndIncrement();
        themes.add(new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnail()));
        return id;
    }

    @Override
    public List<Theme> findAll() {
        return Collections.unmodifiableList(themes);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return themes.stream()
                .filter(theme -> theme.getId().equals(id))
                .findAny();
    }

    @Override
    public void deleteById(Long id) {
        Optional<Theme> findTheme = themes.stream()
                .filter(theme -> theme.getId().equals(id))
                .findAny();
        findTheme.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 id입니다."));
        themes.remove(findTheme.get());
    }
}
