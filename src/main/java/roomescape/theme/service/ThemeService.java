package roomescape.theme.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.service.dto.ThemeCommand;
import roomescape.theme.service.dto.ThemeResult;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ThemeResult save(ThemeCommand command) {
        Theme newTheme = Theme.of(command.name(), command.description(), command.thumbnailUrl());
        try {
            Theme saved = themeRepository.save(newTheme);
            return ThemeResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ThemeErrorCode.DUPLICATE_THEME);
        }
    }

    public List<ThemeResult> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResult::from)
                .toList();
    }

    public Theme findById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND)
                );
    }

    @Transactional
    public void delete(Long id) {
        Theme deleteTarget = findById(id);

        try {
            themeRepository.delete(deleteTarget);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ThemeErrorCode.THEME_IN_USE);
        }
    }
}
