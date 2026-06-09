package roomescape.theme.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        Theme newTheme = Theme.of(command.name(), command.description(), command.thumbnailUrl());
        Theme saved = themeRepository.save(newTheme);
        return ThemeResult.from(saved);
    }

    public List<ThemeResult> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResult::from)
                .toList();
    }

    public Theme findById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(ThemeErrorCode.THEME_NOT_FOUND)
                );
    }

    @Transactional
    public void delete(long id) {
        Theme deleteTarget = findById(id);
        themeRepository.delete(deleteTarget);
    }
}
