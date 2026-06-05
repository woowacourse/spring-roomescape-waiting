package roomescape.theme.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ThemeErrorCode;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.domain.PopularThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeSortType;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeReference reservationReference;
    private final ThemeRepository themeRepository;
    private final PopularThemeRepository popularThemeRepository;

    public Theme save(ThemeCreateCommand createCommand) {
        Theme theme = Theme.create(
                createCommand.name(),
                createCommand.description(),
                createCommand.thumbnail()
        );
        return themeRepository.save(theme);
    }

    public Optional<Theme> findById(Long id) {
        return themeRepository.findById(id);
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findTopNByPeriod(LocalDate startAt, LocalDate endAt, String sortBy, Long limit) {
        ThemeSortType sortType = ThemeSortType.valueOf(sortBy.toUpperCase());
        return popularThemeRepository.findTopNByPeriod(startAt, endAt, sortType, limit);
    }

    public void deleteById(Long id) {
        themeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ThemeErrorCode.THEME_NOT_FOUND, id));
        reservationReference.validateThemeNotReferenced(id);
        themeRepository.deleteById(id);
    }
}
