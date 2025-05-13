package roomescape.theme.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.common.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new CopyOnWriteArrayList<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public List<Theme> findAll() {
        return new CopyOnWriteArrayList<>(themes);
    }

    @Override
    public Optional<Theme> findById(ThemeId id) {
        try {
            return Optional.of(themes.get((int) (id.getValue() - 1)));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    @Override
    public Theme save(Theme theme) {
        Theme saved = Theme.withId(
                ThemeId.from(index.getAndIncrement()),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail());

        themes.add(saved);

        return saved;
    }

    @Override
    public void deleteById(ThemeId id) {
        Theme targetTheme = themes.stream()
                .filter(theme -> Objects.equals(theme.getId(), id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("데이터베이스에 해당 id가 존재하지 않습니다."));

        themes.remove(targetTheme);
    }
}
