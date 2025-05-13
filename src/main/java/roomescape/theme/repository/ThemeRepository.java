package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.PopularThemeResponse;

public interface ThemeRepository {

    Theme save(final Theme theme);

    List<Theme> findAll();

    List<PopularThemeResponse> findAllPopular();

    int deleteById(final Long id);

    Optional<Theme> findById(final Long id);
}
