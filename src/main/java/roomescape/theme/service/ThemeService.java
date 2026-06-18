package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 테마입니다."));
    }

    public List<ThemeResponse> getTopThemes(int limit) {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Long> themeIds = themeRepository.findTopThemeIds(startDate, endDate, PageRequest.of(0, limit));
        Map<Long, Theme> themeMap = themeRepository.findAllByIds(themeIds).stream()
                .collect(Collectors.toMap(Theme::getId, theme -> theme));
        return themeIds.stream()
                .map(themeMap::get)
                .map(ThemeResponse::of)
                .collect(Collectors.toList());
    }
}
