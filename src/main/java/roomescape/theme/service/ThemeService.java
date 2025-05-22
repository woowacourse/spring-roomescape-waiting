package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    @Autowired
    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public ThemeResponse createTheme(ThemeCreateRequest request) {
        Theme theme = themeRepository.save(request.toTheme());
        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> findAll() {
        List<Theme> themes = themeRepository.findAll();
        return toThemeResponses(themes);
    }

    public void deleteThemeById(Long id) {
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> findLimitedThemesByPopularDesc() {
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now().minusDays(1);
        List<Theme> themes = themeRepository.findTopByReservationCountDesc(fromDate, toDate, 10L);
        return toThemeResponses(themes);
    }

    private List<ThemeResponse> toThemeResponses(List<Theme> themes) {
        return themes.stream()
            .map(ThemeResponse::from)
            .toList();
    }

    public Optional<Theme> findById(Long id) {
        return themeRepository.findById(id);
    }
}
