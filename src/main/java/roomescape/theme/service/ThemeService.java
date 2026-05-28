package roomescape.theme.service;

import java.util.List;
import roomescape.global.exception.DuplicateException;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.domain.Theme;



import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.ThemeCommand;

@Transactional(readOnly = true)
@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Theme save(ThemeCommand command) {
        if (themeRepository.existsByName(command.name())) {
            throw new DuplicateException(ThemeErrorCode.DUPLICATE_THEME.getMessage());
        }

        try {
            return themeRepository.save(
                    Theme.of(
                            command.name(),
                            command.description(),
                            command.thumbnailUrl()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException(ThemeErrorCode.DUPLICATE_THEME.getMessage());
        }
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage()));
    }

    @Transactional
    public void deleteById(Long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
        }

        try {
            int affectedRow = themeRepository.deleteById(id);
            int nonAffected = 0;

            if (affectedRow == nonAffected) {
                throw new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND.getMessage());
            }
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ThemeErrorCode.THEME_IN_USE.getMessage());
        }
    }
}
