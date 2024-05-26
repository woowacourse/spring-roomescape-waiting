package roomescape.reservation.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.dto.request.CreateThemeRequest;
import roomescape.reservation.dto.response.CreateThemeResponse;
import roomescape.reservation.dto.response.FindPopularThemesResponse;
import roomescape.reservation.dto.response.FindThemeResponse;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.ThemeRepository;

@Service
@Transactional
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeServiceValidator themeServiceValidator;

    public ThemeService(ThemeRepository themeRepository, ThemeServiceValidator themeServiceValidator) {
        this.themeRepository = themeRepository;
        this.themeServiceValidator = themeServiceValidator;
    }

    public CreateThemeResponse createTheme(CreateThemeRequest createThemeRequest) {
        Theme theme = themeRepository.save(createThemeRequest.toTheme());
        return CreateThemeResponse.from(theme);
    }

    @Transactional(readOnly = true)
    public List<FindThemeResponse> getThemes() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(FindThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FindPopularThemesResponse> getPopularThemes(Pageable pageable) {
        return themeRepository.findAllOrderByReservationCount(pageable).stream()
                .map(FindPopularThemesResponse::from)
                .toList();
    }

    public void deleteById(Long id) {
        themeServiceValidator.validateExistTheme(id);
        themeServiceValidator.validateThemeUsage(id);

        themeRepository.deleteById(id);
    }

}
