package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRequest;
import roomescape.domain.theme.ThemeResponse;
import roomescape.exception.ReferencedDataException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ThemeQueryingDao;
import roomescape.repository.ThemeUpdatingDao;

import java.util.List;

@Service
public class ThemeService {

    private final ThemeQueryingDao themeQueryingDao;
    private final ThemeUpdatingDao themeUpdatingDao;

    public ThemeService(ThemeQueryingDao themeQueryingDao, ThemeUpdatingDao themeUpdatingDao) {
        this.themeQueryingDao = themeQueryingDao;
        this.themeUpdatingDao = themeUpdatingDao;
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        Long id = themeUpdatingDao.insert(themeRequest);
        return ThemeResponse.from(new Theme(id, themeRequest.name(), themeRequest.description(), themeRequest.url()));
    }

    public List<ThemeResponse> findAll() {
        return themeQueryingDao.findAllTheme().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findPopularTheme() {
        return themeQueryingDao.findAllByTopTheme().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        try {
            themeUpdatingDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReferencedDataException();
        }
    }
}
