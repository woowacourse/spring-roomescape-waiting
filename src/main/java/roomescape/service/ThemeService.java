package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ThemeRanking;
import roomescape.dto.request.CreateThemeRequest;
import roomescape.entity.ConfirmedReservation;
import roomescape.entity.Reservation;
import roomescape.entity.Theme;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.repository.ConfirmReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int THEME_RANKING_END_RANGE = 7;
    private static final int THEME_RANKING_START_RANGE = 1;

    private final ConfirmReservationRepository confirmReservationRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(ConfirmReservationRepository confirmReservationRepository, ThemeRepository themeRepository) {
        this.confirmReservationRepository = confirmReservationRepository;
        this.themeRepository = themeRepository;
    }

    public Theme addTheme(CreateThemeRequest request) {
        Theme theme = request.toTheme();
        boolean existsByName = themeRepository.existsByName(theme.getName());
        if (existsByName){
            throw new InvalidThemeException("중복된 테마 이름입니다.");
        }
        return themeRepository.save(theme);
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public void deleteThemeById(long id) {
        if (confirmReservationRepository.existsByThemeId(id)) {
            throw new InvalidThemeException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<Theme> getRankingThemes(LocalDate originDate) {
        LocalDate end = originDate.minusDays(THEME_RANKING_START_RANGE);
        LocalDate start = end.minusDays(THEME_RANKING_END_RANGE);

        List<ConfirmedReservation> inRangeReservations = confirmReservationRepository.findAllByDateBetween(start, end);

        ThemeRanking themeRanking = new ThemeRanking(inRangeReservations);
        return themeRanking.getAscendingRanking();
    }
}
