package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.request.CreateThemeRequest;
import roomescape.entity.Reservation;
import roomescape.entity.Theme;
import roomescape.domain.ThemeRanking;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int THEME_RANKING_END_RANGE = 7;
    private static final int THEME_RANKING_START_RANGE = 1;

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public Theme addTheme(CreateThemeRequest request) {
        Theme theme = request.toTheme();
        return themeRepository.save(theme);
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public void deleteThemeById(long id) {
        if (reservationRepository.existsByTheme_Id(id)) {
            throw new InvalidThemeException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<Theme> getRankingThemes(LocalDate originDate) {
        LocalDate end = originDate.minusDays(THEME_RANKING_START_RANGE);
        LocalDate start = end.minusDays(THEME_RANKING_END_RANGE);

        List<Reservation> inRangeReservations = reservationRepository.findAllByDateBetween(end, start);

        ThemeRanking themeRanking = new ThemeRanking(inRangeReservations);
        return themeRanking.getAscendingRanking();
    }
}
