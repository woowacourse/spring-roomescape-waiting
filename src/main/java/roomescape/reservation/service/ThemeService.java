package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.response.PopularThemeResponse;
import roomescape.reservation.dto.request.ThemeCreateRequest;
import roomescape.reservation.dto.response.ThemeResponse;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int POPULAR_THEME_SIZE = 10;
    private static final int POPULAR_THEME_LAST_DAYS = 7;

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Long save(ThemeCreateRequest themeCreateRequest) {
        themeRepository.findByThemeName(themeCreateRequest.name())
                .ifPresent(empty -> {
                    throw new IllegalArgumentException("이미 존재하는 테마 이름입니다.");
                });

        Theme theme = themeCreateRequest.toTheme();

        return themeRepository.save(theme).getId();
    }

    public ThemeResponse findById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        return new ThemeResponse(theme);
    }

    public List<ThemeResponse> findAll() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<PopularThemeResponse> findThemesLastDaysLimitTen() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysBefore = today.minusDays(POPULAR_THEME_LAST_DAYS);

        List<Theme> popularTheme = themeRepository.findPopularThemesWithPagination(today, sevenDaysBefore,
                PageRequest.of(0, POPULAR_THEME_SIZE));

        return popularTheme.stream()
                .map(PopularThemeResponse::new)
                .toList();
    }

    public void delete(Long id) {
        themeRepository.deleteById(id);
    }
}
