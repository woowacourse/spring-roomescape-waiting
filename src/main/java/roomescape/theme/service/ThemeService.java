package roomescape.theme.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.repository.ThemeRepository;
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
        validateThemeNameUniqueness(command.name());
        Theme newTheme = Theme.of(command.name(), command.description(), command.thumbnailUrl());

        try {
            Theme saved = themeRepository.save(newTheme);
            return ThemeResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ThemeErrorCode.DUPLICATE_THEME.getMessage());
        }
    }

    private void validateThemeNameUniqueness(String name) {
        if (themeRepository.existsByName(name)) {
            throw new ConflictException(ThemeErrorCode.DUPLICATE_THEME.getMessage());
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
                        () -> new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage())
                );
    }

    @Transactional
    public void deleteById(Long id) {
        try {
            int affectedRow = themeRepository.deleteById(id);
            if (affectedRow == 0) {
                throw new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
            }
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ThemeErrorCode.THEME_IN_USE.getMessage());
        }
    }
}
