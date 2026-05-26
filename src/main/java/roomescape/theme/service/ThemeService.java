package roomescape.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.dao.ThemeDAO;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;

@Service
public class ThemeService {

    private final ThemeDAO themeDAO;

    public ThemeService(ThemeDAO themeDAO) {
        this.themeDAO = themeDAO;
    }

    public List<Theme> findAll() {
        return themeDAO.findAll();
    }

    public ThemeResponse create(ThemeCreateRequest request) {
        return ThemeResponse.from(themeDAO.insert(request));
    }

    public void delete(long id) {
        boolean deleted = themeDAO.delete(id);
        if (!deleted) {
            throw new IllegalArgumentException("삭제할 테마를 조회하지 못했습니다. id = " + id);
        }
    }
}
