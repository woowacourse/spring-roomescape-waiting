package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.ThemeRepository;
import roomescape.validation.ThemeValidator;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeValidator themeValidator;

    public ThemeService(ThemeRepository themeRepository, ThemeValidator themeValidator) {
        this.themeRepository = themeRepository;
        this.themeValidator = themeValidator;
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        Theme theme = themeRequest.toEntity();
        Theme createdTheme = themeRepository.save(theme);
        return ThemeResponse.from(createdTheme);
    }

    public void delete(Long id) {
        Theme theme = themeRepository.getThemeById(id);
        themeValidator.validateExistReservation(theme);
        themeRepository.deleteById(id);
    }
}
