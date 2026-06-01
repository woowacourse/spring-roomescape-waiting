package roomescape.theme.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.global.exception.ThemeErrorCode;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> store = new HashMap<>();
    private long sequence = 0L;

    @Override
    public Theme save(Theme theme) {
        if (theme.getId() == null) {
            Theme saved = Theme.createRow(++sequence, theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
            store.put(saved.getId(), saved);
            return saved;
        }

        store.put(theme.getId(), theme);

        return theme;
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Theme> findAll() {
        return store.values().stream().toList();
    }

    @Override
    public void deleteById(Long id) {
        Theme removed = store.remove(id);
        if (removed == null) {
            throw new EntityNotFoundException(ThemeErrorCode.THEME_NOT_FOUND);
        }
    }

    public void clear() {
        store.clear();
        sequence = 0L;
    }
}
