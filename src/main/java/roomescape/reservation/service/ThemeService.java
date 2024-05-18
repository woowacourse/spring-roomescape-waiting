package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.PopularThemeResponse;
import roomescape.reservation.dto.ThemeCreateRequest;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int POPULAR_THEME_SIZE = 10;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Long save(ThemeCreateRequest themeCreateRequest) {
        themeRepository.findByThemeName(themeCreateRequest.name())
                .ifPresent(empty -> {
                    throw new IllegalArgumentException("이미 존재하는 테마 이름입니다.");
                });

        Theme theme = themeCreateRequest.toTheme();

        return themeRepository.save(theme).getId();
    }

    public ThemeResponse findById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        return ThemeResponse.toResponse(theme);
    }

    public List<ThemeResponse> findAll() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::toResponse)
                .toList();
    }

    public List<PopularThemeResponse> findPopularThemeBetweenWeekLimitTen() {
        List<Theme> themes = getThemeBetweenWeek();

        Theme theme = new Theme();
        List<Theme> popularTheme = theme.getPopularTheme(themes);

        return popularTheme.stream()
                .limit(10)
                .map(PopularThemeResponse::toResponse)
                .collect(Collectors.toList());
    }

    private List<Theme> getThemeBetweenWeek() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysBefore = today.minusDays(7);

        return reservationRepository.findByDateBetween(sevenDaysBefore, today).stream()
                .map(Reservation::getTheme)
                .toList();
    }

    public void delete(Long id) {
        themeRepository.deleteById(id);
    }
}
