package roomescape.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;

@Service
public class ThemeService {

    private final ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<Theme> findAll() {
        return themeDao.findAll();
    }

    public ThemeResponse create(ThemeCreateRequest request) {
        return ThemeResponse.from(themeDao.insert(request));
    }

    public Theme findById(Long id) {
        return themeDao.findById(id);
    }

    public void delete(long id) {
        themeDao.delete(id);
    }
}
