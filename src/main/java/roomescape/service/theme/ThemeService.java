package roomescape.service.theme;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.service.theme.dto.ThemeRequest;
import roomescape.service.theme.dto.ThemeResponse;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {
    private static final long LIMIT = 10;
    private static final LocalDate START_DATE = LocalDate.now().minusDays(7);
    private static final LocalDate END_DATE = LocalDate.now();

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        Theme theme = themeRequest.toTheme();
        validateDuplicated(theme.getName());
        Theme newTheme = themeRepository.save(theme);
        return new ThemeResponse(newTheme);
    }

    private void validateDuplicated(ThemeName name) {
        if (themeRepository.existsByName(name)) {
            throw new InvalidReservationException("이미 존재하는 테마 이름입니다.");
        }
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public void deleteById(long id) {
        validateByReservation(id);
        themeRepository.deleteById(id);
    }

    private void validateByReservation(long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new InvalidReservationException("해당 테마로 예약이 존재해서 삭제할 수 없습니다.");
        }
    }

    public List<ThemeResponse> findPopularThemes() {
        List<Theme> themes = themeRepository.findByReservationTermAndLimit(START_DATE, END_DATE, LIMIT);
        return themes.stream().map(ThemeResponse::new).toList();
    }
}
