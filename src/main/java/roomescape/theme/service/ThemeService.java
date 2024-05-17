package roomescape.theme.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.exceptions.DuplicationException;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeJpaRepository;

// TODO: 테스트 추가
@Service
public class ThemeService {

    private final ThemeJpaRepository themeJpaRepository;

    public ThemeService(ThemeJpaRepository themeJpaRepository) {
        this.themeJpaRepository = themeJpaRepository;
    }

    public ThemeResponse addTheme(ThemeRequest themeRequest) {
        validateDuplicatedName(themeRequest);
        try {
            Theme theme = themeJpaRepository.save(themeRequest.toTheme());
            return new ThemeResponse(theme);
        } catch (DuplicateKeyException e) {
            throw new DuplicationException("이미 존재하는 테마 이름입니다.");
        }
    }

    private void validateDuplicatedName(ThemeRequest themeRequest) {
        if (themeJpaRepository.existsByName(new Name(themeRequest.name()))) {
            throw new DuplicationException("이미 존재하는 테마 이름입니다.");
        }
    }

    public List<ThemeResponse> findTrendingThemes(Long limit) {
        LocalDate now = LocalDate.now();
        LocalDate trendingStatsStart = now.minusDays(7);
        LocalDate trendingStatsEnd = now.minusDays(1);

        List<Theme> mostReservedThemesBetweenDates = themeJpaRepository.findTrendingThemesBetweenDates(
                trendingStatsStart, trendingStatsEnd, PageRequest.of(0, Math.toIntExact(limit)));
        return mostReservedThemesBetweenDates
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> findThemes() {
        List<ThemeResponse> themeResponses = new ArrayList<>();
        for (Theme theme : themeJpaRepository.findAll()) {
            themeResponses.add(new ThemeResponse(theme));
        }
        return themeResponses;
    }

    public void deleteTheme(Long id) {
        themeJpaRepository.deleteById(id);
    }
}
