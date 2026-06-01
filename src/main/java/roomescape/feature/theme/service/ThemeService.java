package roomescape.feature.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.feature.theme.dto.command.ThemeCreateCommand;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.error.type.ThemeErrorType;
import roomescape.feature.theme.mapper.ThemeMapper;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.global.error.exception.GeneralException;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeMapper themeMapper;

    public ThemeService(ThemeRepository themeRepository, ThemeMapper themeMapper) {
        this.themeRepository = themeRepository;
        this.themeMapper = themeMapper;
    }

    public List<ThemeResponseDto> getThemes() {
        return convertThemesToDto(themeRepository.findAllByNotDeleted());
    }

    private List<ThemeResponseDto> convertThemesToDto(List<Theme> themes) {
        return themes.stream()
            .map(themeMapper::toResponseDto)
            .toList();
    }

    public List<ThemeResponseDto> getPopularThemes() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today.minusDays(1);

        return convertThemesToDto(
            themeRepository.findPopularThemesDateBetween(startDate, endDate, 10));
    }

    @Transactional
    public ThemeResponseDto saveTheme(ThemeCreateCommand command) {
        if (themeRepository.existsThemeByNameAndNotDeleted(command.name().value())) {
            throw new GeneralException(ThemeErrorType.ALREADY_EXIST_THEME);
        }

        try {
            Theme theme = Theme.create(command.name().value(), command.description().value(), command.imageUrl().value());
            return themeMapper.toResponseDto(themeRepository.save(theme));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ThemeErrorType.ALREADY_EXIST_THEME);
        }
    }

    @Transactional
    public void deleteThemeById(Long id) {
        if (!themeRepository.existsThemeByIdAndNotDeleted(id)) {
            throw new GeneralException(ThemeErrorType.THEME_NOT_FOUND);
        }

        themeRepository.deleteThemeById(id);
    }
}
