package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.repository.ThemeDao;

@Service
@Transactional
@RequiredArgsConstructor
public class ThemeCommandService {

    private final ThemeDao themeDao;

    public Theme create(String name, String thumbnailUrl, String description) {
        return themeDao.save(new Theme(null, name, thumbnailUrl, description));
    }

    public void delete(long themeId) {
        try {
            themeDao.deleteById(themeId);
        } catch (DataIntegrityViolationException e) {
            throw new DeletionNotAllowedException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
    }
}
