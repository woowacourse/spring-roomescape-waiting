package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dto.ThemeRequest;
import roomescape.repository.ThemeDao;

@Service
public class AdminThemeService {

    private final ThemeDao themeDao;

    public AdminThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public Long save(ThemeRequest request) {
        return themeDao.save(request.name(), request.description(), request.thumbnailUrl());
    }

    public void delete(long id) {
        themeDao.delete(id);
    }
}
