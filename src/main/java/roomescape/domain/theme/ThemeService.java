package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.dto.AdminThemeResponse;
import roomescape.domain.theme.dto.ThemeCreationRequest;
import roomescape.domain.theme.dto.ThemeCreationResponse;
import roomescape.domain.theme.dto.ThemeRankResponse;
import roomescape.domain.theme.dto.ThemeResponse;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int RANK_LIMIT = 10;
    private static final int RANK_DAYS_START = 7;
    private static final int RANK_DAYS_END = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public List<AdminThemeResponse> getAllThemeForAdmin() {
        return themeRepository.findAll().stream()
            .map(AdminThemeResponse::from)
            .toList();
    }

    public ThemeCreationResponse createTheme(ThemeCreationRequest request) {
        Theme theme = themeRepository.save(request.toEntity());
        return ThemeCreationResponse.from(theme);
    }

    public void deleteTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ThemeErrorCode.THEME_NOT_EXIST));
        if (reservationRepository.countByThemeId(id) > 0) {
            throw new RoomescapeException(ThemeErrorCode.THEME_IN_USE);
        }
        themeRepository.delete(theme);
    }

    public List<ThemeResponse> getAllTheme() {
        return themeRepository.findAll().stream()
            .map(ThemeResponse::from)
            .toList();
    }

    public List<ThemeRankResponse> getThemeRank() {
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.minusDays(RANK_DAYS_START);
        LocalDate endDay = today.minusDays(RANK_DAYS_END);
        List<Theme> populateThemes = themeRepository.findPopularThemes(RANK_LIMIT, startDay, endDay);
        return populateThemes.stream()
            .map(ThemeRankResponse::from)
            .toList();
    }
}
