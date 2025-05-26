package roomescape.infrastructure.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.repository.dao.JpaThemeDao;

import java.util.Optional;

@Primary
@Repository
public class JpaThemes implements Themes {

    private final JpaThemeDao dao;

    public JpaThemes(JpaThemeDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(Theme theme) {
        dao.save(theme);
    }

    @Override
    public Optional<Theme> findById(Id themeId) {
        return dao.findById(themeId);
    }

    @Override
    public boolean existById(Id themeId) {
        return dao.existsById(themeId);
    }

    @Override
    public void deleteById(Id themeId) {
        dao.deleteById(themeId);
    }
}
