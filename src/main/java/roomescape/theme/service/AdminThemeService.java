package roomescape.theme.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.AdminThemeRequest;
import roomescape.theme.dto.AdminThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class AdminThemeService {

    private final ThemeRepository themeRepository;

    public AdminThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public AdminThemeResponse createTheme(AdminThemeRequest request) {
        Theme theme = Theme.of(request.name(), request.description(), request.imageUrl(), request.price());
        Theme saved = themeRepository.save(theme);
        return AdminThemeResponse.from(saved);
    }

    public List<AdminThemeResponse> getAllThemes() {
        return themeRepository.findAll().stream()
                .map(AdminThemeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTheme(Long id) {
        if (themeRepository.hasReservation(id)) {
            throw new BusinessException(ErrorCode.THEME_HAS_RESERVATION);
        }
        themeRepository.deleteById(id);
    }
}