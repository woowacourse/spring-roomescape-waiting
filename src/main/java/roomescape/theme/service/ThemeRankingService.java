package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.dao.ThemeDAO;
import roomescape.theme.dto.response.ReservedThemeResponse;

@Service
public class ThemeRankingService {

    private final ThemeDAO themeDAO;

    public ThemeRankingService(ThemeDAO themeDAO) {
        this.themeDAO = themeDAO;
    }

    public List<ReservedThemeResponse> findMostReserved(long limit, LocalDate startDate, LocalDate endDate) {
        if (endDate != null) {
            return themeDAO.findMostReserved(limit, startDate, endDate);
        }

        return themeDAO.findMostReserved(limit, startDate, LocalDate.now().minusDays(1));
    }
}
