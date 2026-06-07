package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.dto.ThemeResponse;

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

        List<Long> top10Ids = ThemeRanking.from(
                reservationRepository.findThemeIdsByDateRange(startDate, endDate)
        ).topThemeIds(10);

        Map<Long, Theme> themeMap = themeRepository.findByIds(top10Ids).stream()
                .collect(Collectors.toMap(Theme::getId, t -> t));

        return top10Ids.stream()
                .map(themeMap::get)
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
