package roomescape.theme.application.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.ConflictException;
import roomescape.global.NotFoundException;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.exception.ThemeErrorMessage;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.presentation.dto.ThemeResponse;

@RequiredArgsConstructor
@Transactional
@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    @Transactional(readOnly = true)
    public ThemeResponse findById(Long id) {
        return ThemeResponse.from(themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ThemeErrorMessage.THEME_NOT_FOUND, id)));
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findPopularThemes(LocalDate startAt, LocalDate endAt, int limit) {
        return themeRepository.findSortedPopularThemes(startAt, endAt, limit)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse save(ThemeCreateCommand request) {
        Theme theme = request.toEntity();
        validateDuplicateTheme(theme);
        return ThemeResponse.from(themeRepository.save(theme));
    }

    public void delete(long id) {
        themeRepository.delete(id);
    }

    private void validateDuplicateTheme(Theme theme) {
        if (themeRepository.existsByNameAndDescription(theme)) {
            throw new ConflictException(ThemeErrorMessage.DUPLICATE_THEME);
        }
    }
}
