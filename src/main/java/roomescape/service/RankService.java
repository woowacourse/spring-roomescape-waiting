package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class RankService {

    private final ThemeRepository themeRepository;

    public RankService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> findPopularThemes() {
        LocalDate currentDate = LocalDate.now();

        Pageable pageable = PageRequest.of(0, 10);
        List<Theme> topTenByDate = themeRepository.findThemesWithReservationsBetweenDates(
                currentDate.minusWeeks(1),
                currentDate.minusDays(1),
                pageable
        );
        return topTenByDate.stream().map(ThemeResponse::from).toList();
    }
}
