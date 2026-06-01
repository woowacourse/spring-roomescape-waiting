package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.dao.ThemeDao;
import roomescape.domain.theme.Theme;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.service.command.ThemeCommand;

@Service
@RequiredArgsConstructor
public class ThemeCommandService {

    private final ThemeDao themeDao;

    public Theme create(ThemeCommand command) {
        return themeDao.save(
                Theme.create(
                        command.name(),
                        command.thumbnailUrl(),
                        command.description())
        );
    }

    public void delete(long themeId) {
        try {
            Theme theme = themeDao.findById(themeId);
            themeDao.delete(theme);
        } catch (DataIntegrityViolationException e) {
            throw new DeletionNotAllowedException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
    }
}
