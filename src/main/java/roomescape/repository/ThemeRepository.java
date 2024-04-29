package roomescape.repository;

import java.util.List;
import roomescape.domain.theme.Theme;

public interface ThemeRepository {

    Theme save(Theme theme);

    int deleteById(Long id);

    List<Theme> findAll();

    Theme findById(Long id);

    Boolean isExists(String name);

    List<Theme> findPopular(int start, int end, int count);
}
