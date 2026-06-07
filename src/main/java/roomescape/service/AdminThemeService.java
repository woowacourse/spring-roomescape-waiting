package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import roomescape.dto.ThemeRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;
import roomescape.infrastructure.dao.ThemeDao;

@Service
public class AdminThemeService {

    private final ThemeDao themeDao;

    public AdminThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public Long save(ThemeRequest request) {
        try {
            return themeDao.save(request.name(), request.description(), request.thumbnailUrl());
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_THEME);
        }
    }

    public void delete(long id) {
        try {
            themeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_EXISTS_THEME);
        }
    }
}
