package roomescape.theme.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFactory;
import roomescape.theme.dto.AdminThemeRequest;
import roomescape.theme.dto.AdminThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class AdminThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeFactory themeFactory;

    public AdminThemeService(ThemeRepository themeRepository, ThemeFactory themeFactory) {
        this.themeRepository = themeRepository;
        this.themeFactory = themeFactory;
    }

    @Transactional
    public AdminThemeResponse createTheme(AdminThemeRequest request) {
        Theme theme = themeFactory.create(request.name(), request.description(), request.imageUrl());
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
        if (themeRepository.existsReservationByThemeId(id)) {
            throw new BusinessException(ErrorCode.THEME_HAS_RESERVATION);
        }
        themeRepository.deleteById(id);
    }
}