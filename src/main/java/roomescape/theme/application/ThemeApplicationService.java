package roomescape.theme.application;

import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationslot.application.ReservationSlotDataService;
import roomescape.reservationtime.exception.ReservationTimeInUseException;
import roomescape.theme.domain.Theme;
import roomescape.theme.presentation.dto.request.ThemeCreateWebRequest;
import roomescape.theme.presentation.dto.response.ThemeWebResponse;

@Service
@Transactional
public class ThemeApplicationService {

    private final ThemeDataService themeDataService;
    private final ReservationSlotDataService reservationSlotDataService;
    private final Clock clock;

    public ThemeApplicationService(final ThemeDataService themeDataService,
                                   final ReservationSlotDataService reservationSlotDataService,
                                   final Clock clock) {
        this.themeDataService = themeDataService;
        this.reservationSlotDataService = reservationSlotDataService;
        this.clock = clock;
    }

    public ThemeWebResponse create(final ThemeCreateWebRequest request) {
        Theme theme = themeDataService.save(request.toTheme());
        return ThemeWebResponse.from(theme);
    }

    public List<ThemeWebResponse> findAll() {
        return themeDataService.findAll().stream()
                .map(ThemeWebResponse::from)
                .toList();
    }

    public List<ThemeWebResponse> findPopular(int days, int limit) {
        Page<Theme> popularThemes = themeDataService.findPopularThemes(clock, days, limit);
        return popularThemes.getContent().stream()
                .map(ThemeWebResponse::from)
                .toList();
    }

    public void delete(Long id) {
        validateExist(id);
        themeDataService.deleteById(id);
    }

    private void validateExist(final Long id) {
        if (reservationSlotDataService.existsByThemeId(id)) {
            throw new ReservationTimeInUseException("해당 테마에 대한 예약이 존재하여 삭제할 수 없습니다.");
        }
    }
}
