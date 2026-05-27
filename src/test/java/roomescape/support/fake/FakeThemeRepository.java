package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.theme.dto.ThemeRankResult;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> storage = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ThemeRankResult> findPopularThemes(int rankLimit, LocalDate startDay, LocalDate today) {
        return List.of();
    }

    @Override
    public Theme save(Theme theme) {
        Long id = theme.getId();
        if (id == null) {
            id = sequence++;
        } else {
            sequence = Math.max(sequence, id + 1);
        }
        Theme savedTheme = Theme.of(id, theme.getName(), theme.getContent(), theme.getUrl());
        storage.put(id, savedTheme);
        return savedTheme;
    }

    @Override
    public int deleteById(Long id) {
        Theme removedTheme = storage.remove(id);
        if (removedTheme == null) {
            return 0;
        }
        return 1;
    }
}
