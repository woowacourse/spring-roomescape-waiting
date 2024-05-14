package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.NotFoundException;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public ThemeResponse addTheme(ThemeRequest themeRequest) {
        validateDuplicatedName(themeRequest);
        try {
            Theme theme = themeRepository.save(themeRequest.toTheme());
            return new ThemeResponse(theme);
        } catch (DuplicateKeyException e) {
            throw new DuplicationException("이미 존재하는 테마 이름입니다.");
        }
    }

    private void validateDuplicatedName(ThemeRequest themeRequest) {
        if (themeRepository.hasTheme(new Name(themeRequest.name()))) {
            throw new DuplicationException("이미 존재하는 테마 이름입니다.");
        }
    }

    public ThemeResponse getTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 id입니다. theme_id = " + id));

        return new ThemeResponse(theme);
    }

    public List<ThemeResponse> findTrendingThemes(Long limit) {
        LocalDate now = LocalDate.now();
        LocalDate trendingStatsStart = now.minusDays(7);
        LocalDate trendingStatsEnd = now.minusDays(1);

        return themeRepository.findTrendings(trendingStatsStart, trendingStatsEnd, limit)
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> findThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public void deleteTheme(Long id) {
        themeRepository.delete(id);
    }
}
