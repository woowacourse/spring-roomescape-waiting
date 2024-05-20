package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.model.RoomEscapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRankResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.exception.ThemeExceptionCode;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    public static final int NUMBER_OF_ONE_WEEK = 7;
    public static final int TOP_THEMES_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse addTheme(ThemeRequest themeRequest) {
        themeRepository.existsById(1L);
        Theme theme = themeRequest.toTheme();
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.fromTheme(savedTheme);
    }

    public List<ThemeRankResponse> findRankedThemes(LocalDate today) {
        LocalDate beforeOneWeek = today.minusDays(NUMBER_OF_ONE_WEEK);

        List<Theme> rankedThemes = themeRepository.findLimitedAllByDateOrderByThemeIdCount(beforeOneWeek, today,
                TOP_THEMES_LIMIT);

        return rankedThemes.stream()
                .map(ThemeRankResponse::fromTheme)
                .toList();
    }

    public List<ThemeResponse> findThemes() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::fromTheme)
                .toList();
    }

    public void removeTheme(long id) {
        List<Reservation> reservation = reservationRepository.findByThemeId(id);

        if (!reservation.isEmpty()) {
            throw new RoomEscapeException(ThemeExceptionCode.USING_THEME_RESERVATION_EXIST);
        }
        themeRepository.deleteById(id);
    }
}
