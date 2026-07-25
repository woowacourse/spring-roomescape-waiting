package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Theme;

public class FakeThemeRepository extends AbstractFakeRepository<Theme, Long> implements ThemeRepository {

    @Override
    protected Long getId(Theme entity) {
        return entity.getId();
    }

    @Override
    protected Theme withId(Theme entity, Long id) {
        return new Theme(id, entity.getName(), entity.getDescription(), entity.getThumbnailUrl());
    }

    @Override
    public List<Theme> findPopularThemes(Long topCount, LocalDate fromDate, LocalDate toDate) {
        return List.of();
    }
}
