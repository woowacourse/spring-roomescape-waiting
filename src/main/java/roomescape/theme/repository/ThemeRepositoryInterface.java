package roomescape.theme.repository;

import java.util.List;
import roomescape.theme.domain.Theme;

public interface ThemeRepositoryInterface {
    boolean existsByName(String name);

    List<Theme> findAll();

    Theme findById(Long id);

    Theme save(Theme theme);

    void deleteById(Long id);
}