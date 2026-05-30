package roomescape.theme.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import roomescape.exception.AppException;
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

    public void delete(long id) {
        boolean deleted = themeDao.delete(id);
        if (!deleted) {
            throw new AppException(HttpStatus.NOT_FOUND, "삭제할 테마를 조회하지 못했습니다. id = " + id);
        }
    }
}
