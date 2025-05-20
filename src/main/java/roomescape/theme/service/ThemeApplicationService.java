package roomescape.theme.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import roomescape.reservation.service.ReservationDomainService;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;

@Service
public class ThemeApplicationService {

    private final ThemeDomainService themeDomainService;
    private final ReservationDomainService reservationDomainService;

    public ThemeApplicationService(final ThemeDomainService themeDomainService,
                                   final ReservationDomainService reservationDomainService) {
        this.themeDomainService = themeDomainService;
        this.reservationDomainService = reservationDomainService;
    }

    public List<ThemeResponse> getThemes() {
        return themeDomainService.getThemes().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void delete(Long id) {
        if (reservationDomainService.existsByThemeId(id)) {
            throw new ReservationTimeInUseException("해당 테마에 대한 예약이 존재하여 삭제할 수 없습니다.");
        }
        themeDomainService.deleteById(id);
    }

    public ThemeResponse create(final ThemeCreateRequest request) {
        Theme theme = themeDomainService.save(request.toTheme());
        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> getPopularThemes(int days, int limit) {
        Page<Theme> popularThemes = themeDomainService.findPopularThemes(days, limit);
        return popularThemes.getContent().stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
