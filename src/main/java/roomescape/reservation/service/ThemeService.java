package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.PopularThemeResponse;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.dto.ThemeSaveRequest;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Long save(ThemeSaveRequest themeSaveRequest) {
        themeRepository.findByName(themeSaveRequest.name()).ifPresent(empty -> {
            throw new IllegalArgumentException("이미 존재하는 테마 이름입니다.");
        });

        Theme theme = themeSaveRequest.toTheme();

        return themeRepository.save(theme);
    }

    public ThemeResponse findById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        return ThemeResponse.toResponse(theme);
    }

    public List<ThemeResponse> findAll() {
        List<Theme> themes = themeRepository.findAll();

        return themes.stream()
                .map(ThemeResponse::toResponse)
                .toList();
    }

    public List<PopularThemeResponse> findThemesDescOfLastWeekForLimitCount(int limitCount) {
        List<Theme> popularTheme = themeRepository.findPopularThemesDescOfLastWeekForLimit(limitCount);
        return popularTheme.stream()
                .map(PopularThemeResponse::toResponse)
                .toList();
    }

    public void delete(Long id) {
        List<Theme> themes = themeRepository.findThemesThatReservationReferById(id);
        if (!themes.isEmpty()) {
            throw new IllegalArgumentException("해당 테마로 예약된 내역이 있습니다.");
        }
        themeRepository.delete(id);
    }
}
