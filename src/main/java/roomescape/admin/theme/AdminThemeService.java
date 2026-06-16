package roomescape.admin.theme;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.theme.dto.AdminThemeRequest;
import roomescape.admin.theme.dto.AdminThemeResponse;
import roomescape.admin.theme.dto.AdminThemesResponse;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class AdminThemeService {

    private final AdminThemeRepository adminThemeRepository;
    private final ReservationRepository reservationRepository;

    public AdminThemeService(
            AdminThemeRepository adminThemeRepository,
            ReservationRepository reservationRepository
    ) {
        this.adminThemeRepository = adminThemeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public AdminThemeResponse createTheme(AdminThemeRequest request) {
        validateDuplicateTheme(request.name());
        Theme theme = Theme.of(
                request.name(),
                request.description(),
                request.imageUrl(),
                request.price()
        );

        Theme saved = adminThemeRepository.save(theme);
        return AdminThemeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AdminThemesResponse getAllThemes() {
        List<Theme> themes = adminThemeRepository.findAll();

        return AdminThemesResponse.from(themes);
    }

    @Transactional
    public void deleteTheme(Long themeId) {
        validateThemeId(themeId);
        validateTimeDeletable(themeId);
        try {
            adminThemeRepository.deleteById(themeId);
        } catch (DataIntegrityViolationException e) {
            throw new RoomescapeException(ErrorCode.TIME_DELETE_NOT_ALLOWED);
        }
    }

    private void validateThemeId(Long themeId) {
        if (!adminThemeRepository.existsById(themeId)) {
            throw new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND);
        }
    }

    private void validateTimeDeletable(Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new RoomescapeException(ErrorCode.TIME_DELETE_NOT_ALLOWED);
        }
    }

    private void validateDuplicateTheme(String name) {
        if (adminThemeRepository.existsByName(name)) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_THEME_NAME);
        }
    }
}
