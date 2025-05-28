package roomescape.business.model.repository;

import roomescape.business.model.entity.Theme;
import roomescape.business.model.vo.Id;

import java.util.Optional;

public interface Themes {

    void save(Theme theme);

    Optional<Theme> findById(Id themeId);

    boolean existById(Id themeId);

    void deleteById(Id themeId);
}
