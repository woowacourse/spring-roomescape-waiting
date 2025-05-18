package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.persistence.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.persistence.ThemeRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundThemeException;
import roomescape.service.param.CreateThemeParam;
import roomescape.service.result.ThemeResult;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {

    private static final int RANK_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResult> findAll() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResult::from)
                .toList();
    }

    public Long create(CreateThemeParam createThemeParam) {
        Theme theme = new Theme(createThemeParam.name(), createThemeParam.description(), createThemeParam.thumbnail());
        Theme savedTheme = themeRepository.save(theme);

        return savedTheme.getId();
    }

    public ThemeResult findById(Long id) {
        Theme theme = themeRepository.findById(id).orElseThrow(
                () -> new NotFoundThemeException("id에 해당하는 Theme이 없습니다."));

        return ThemeResult.from(theme);
    }

    public void deleteById(final Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new DeletionNotAllowedException("해당 테마에 예약이 존재합니다.");
        }
        themeRepository.deleteById(themeId);
    }

    public List<ThemeResult> findRankByTheme() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusWeeks(1);
        LocalDate endDate = today.minusDays(1);

        List<Theme> rankForWeek = themeRepository.findRankByDate(startDate, endDate, RANK_LIMIT);

        return rankForWeek.stream()
                .map(ThemeResult::from)
                .toList();
    }
}
