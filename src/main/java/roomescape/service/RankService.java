package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.ThemeRepository;

@Service
public class RankService {

    private final ThemeRepository themeRepository;

    public RankService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> getPopularThemeList(LocalDate dateFrom, LocalDate dateTo) {
        List<Theme> topTenByDate =
                themeRepository.findThemesWithReservationsBetweenDates(dateFrom, dateTo);
        return topTenByDate.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
