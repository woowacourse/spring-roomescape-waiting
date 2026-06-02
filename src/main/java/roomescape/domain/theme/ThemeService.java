package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.dto.ThemeResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getTopThemes() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Long> themeIds = reservationRepository.findThemeIdTop10(startDate, endDate);

        return themeIds.stream()
            .map((themeId) ->
                themeRepository.findById(themeId)
                    .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND)))
            .map(ThemeResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll().stream()
            .map(ThemeResponse::from)
            .toList();
    }
}
