package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.ThemeRequest;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ThemeDao;

@Service
@Transactional(readOnly = true)
public class AdminThemeService {

    private final ThemeDao themeDao;

    public AdminThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    @Transactional
    public Long save(ThemeRequest request) {
        try {
            return themeDao.save(request.name(), request.description(), request.thumbnailUrl());
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_THEME_NAME, "존재하는 테마는 추가할 수 없습니다.");
        }
    }

    @Transactional
    public void delete(long id) {
        try {
            themeDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new RoomescapeException(DomainErrorCode.REFERENTIAL_INTEGRITY, "사용중인 테마는 삭제할 수 없습니다.");
        }
    }
}
