package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.theme.Theme;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.ReferencedDataException;
import roomescape.repository.ThemeQueryDao;
import roomescape.repository.ThemeUpdateDao;

import java.util.List;

@Service
public class ThemeService {

    private final ThemeQueryDao themeQueryDao;
    private final ThemeUpdateDao themeUpdateDao;

    public ThemeService(ThemeQueryDao themeQueryDao, ThemeUpdateDao themeUpdateDao) {
        this.themeQueryDao = themeQueryDao;
        this.themeUpdateDao = themeUpdateDao;
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        Long id = themeUpdateDao.insert(themeRequest);
        return ThemeResponse.from(new Theme(id, themeRequest.name(), themeRequest.description(), themeRequest.url()));
    }

    public List<ThemeResponse> findAll() {
        return themeQueryDao.findAllTheme().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findPopularTheme() {
        return themeQueryDao.findAllByTopTheme().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        try {
            themeUpdateDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReferencedDataException("해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }
}
