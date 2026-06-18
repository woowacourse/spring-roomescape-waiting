package roomescape.domain.theme.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.dto.command.ThemeCreateCommand;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.error.type.ThemeErrorType;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.global.error.exception.GeneralException;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeMapper themeMapper;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository, ThemeMapper themeMapper, Clock clock) {
        this.themeRepository = themeRepository;
        this.themeMapper = themeMapper;
        this.clock = clock;
    }

    public List<ThemeResponseDto> getThemes() {
        return convertThemesToDto(themeRepository.findAllByDeletedAtIsNull());
    }

    private List<ThemeResponseDto> convertThemesToDto(List<Theme> themes) {
        return themes.stream()
            .map(themeMapper::toResponseDto)
            .toList();
    }

    public List<ThemeResponseDto> getPopularThemes() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today.minusDays(1);

        return convertThemesToDto(
            themeRepository.findPopularThemesDateBetween(startDate, endDate, PageRequest.of(0, 10)));
    }

    @Transactional
    public ThemeResponseDto saveTheme(ThemeCreateCommand command) {
        if (themeRepository.existsThemeByNameAndDeletedAtIsNull(command.name())) {
            throw new GeneralException(ThemeErrorType.ALREADY_EXIST_THEME);
        }

        try {
            Theme theme = Theme.create(command.name(), command.description(), command.imageUrl());
            return themeMapper.toResponseDto(themeRepository.save(theme));
        } catch (DuplicateKeyException e) {
            throw new GeneralException(ThemeErrorType.ALREADY_EXIST_THEME);
        }
    }

    @Transactional
    public void deleteThemeById(Long id) {
        if (!themeRepository.existsThemeByIdAndDeletedAtIsNull(id)) {
            throw new GeneralException(ThemeErrorType.THEME_NOT_FOUND);
        }

        themeRepository.deleteThemeById(id);
    }
}
