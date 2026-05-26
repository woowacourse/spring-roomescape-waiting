package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme getById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }

    public List<ThemeResponse> getTopThemes(int limit) {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Long> themeIds = themeRepository.findTopThemeIds(startDate, endDate, limit);
        Map<Long, Theme> themeMap = themeRepository.findAllByIds(themeIds).stream()
                .collect(Collectors.toMap(Theme::getId, theme -> theme));
        return themeIds.stream()
                .map(themeMap::get)
                .map(ThemeResponse::of)
                .collect(Collectors.toList());
    }
}
