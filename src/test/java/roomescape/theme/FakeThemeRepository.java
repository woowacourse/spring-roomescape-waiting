package roomescape.theme;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.dao.EmptyResultDataAccessException;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new ArrayList<>();
    private final List<Long> invokeDeleteId = new ArrayList<>();
    private final List<Theme> returnFindAllOrderByRank = new ArrayList<>();
    private Long NEXT_ID = 1L;

    @Override
    public Long save(final Theme theme) {
        final Theme saveTheme = new Theme(NEXT_ID++, theme.getName(), theme.getDescription(), theme.getThumbnail());
        themes.add(saveTheme);
        return saveTheme.getId();
    }

    @Override
    public Theme findById(final Long id) {
        return themes.stream()
                .filter(theme -> Objects.equals(theme.getId(), id))
                .findAny()
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(themes);
    }

    @Override
    public List<Theme> findAllOrderByRank(final LocalDate from, final LocalDate to, final int size) {
        return new ArrayList<>(returnFindAllOrderByRank).stream()
                .limit(size)
                .collect(Collectors.toList());
    }

    public void stubFindAllOrderByRank(final List<Theme> themes) {
        this.returnFindAllOrderByRank.addAll(themes);
    }

    @Override
    public void deleteById(final Long id) {
        themes.stream()
                .filter(theme -> Objects.equals(theme.getId(), id))
                .findAny()
                .ifPresent(theme -> themes.remove(theme));

        invokeDeleteId.add(id);
    }

    @Override
    public Boolean existsById(final Long id) {
        return themes.stream()
                .anyMatch(theme -> Objects.equals(theme.getId(), id));
    }

    public void clear() {
        invokeDeleteId.clear();
        themes.clear();
        returnFindAllOrderByRank.clear();
        NEXT_ID = 1L;
    }

    public boolean isInvokeDeleteId(final Long id) {
        return invokeDeleteId.stream()
                .anyMatch(themeId -> Objects.equals(themeId, id));
    }
}
