package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ThemeDao;
import roomescape.domain.theme.Theme;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.service.command.ThemeCommand;

@Service
@RequiredArgsConstructor
public class ThemeCommandService {

    private final ThemeDao themeDao;

    @Transactional
    public Theme create(ThemeCommand command) {
        Theme theme = Theme.create(
                command.name(),
                command.thumbnailUrl(),
                command.description()
        );

        Long savedId = themeDao.create(theme);
        return themeDao.findById(savedId)
                .orElseThrow(() -> new ResourceNotFoundException("테마가 정상적으로 생성되지 않았습니다."));
    }

    @Transactional
    public void delete(Long themeId) {
        try {
            Theme theme = themeDao.findById(themeId)
                    .orElseThrow(() -> new ResourceNotFoundException("삭제하려는 테마가 존재하지 않습니다."));
            themeDao.delete(theme);
        } catch (DataIntegrityViolationException e) {
            throw new DeletionNotAllowedException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
    }
}
