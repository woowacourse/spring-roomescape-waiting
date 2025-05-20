package roomescape.infrastructure;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.util.List;
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
    public List<Theme> findAll() {
        return dao.findAll();
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startInclusive, LocalDate endInclusive, int count) {
        PageRequest request = PageRequest.of(0, count);
        return dao.findPopularThemes(startInclusive, endInclusive, request);
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
