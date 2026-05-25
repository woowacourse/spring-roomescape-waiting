package roomescape.theme.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.DuplicateThemeException;
import roomescape.theme.exception.ThemeInUseException;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.ThemeCommand;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Theme registerTheme(ThemeCommand command) {
        if (themeRepository.existByName(command.name())) {
            throw new DuplicateThemeException();
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
            throw new DuplicateThemeException();
        }

    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    @Transactional
    public void removeThemeById(Long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new ThemeNotFoundException();
        }

        try {
            int affectedRow = themeRepository.deleteById(id);
            int nonAffected = 0;

            if (affectedRow == nonAffected) {
                throw new ThemeNotFoundException();
            }
        } catch (DataIntegrityViolationException e) {
            throw new ThemeInUseException();
        }
    }
}
