package roomescape.theme.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.AdminThemeRequest;
import roomescape.theme.dto.AdminThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
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
        if (themeRepository.existsReservationByThemeId(id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }
}
