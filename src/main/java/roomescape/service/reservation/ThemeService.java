package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.ThemeRequest;
import roomescape.service.reservation.dto.ThemeResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ThemeService {
    private static final long MAXIMUM_COUNT = 10;
    private static final String START_DATE = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_DATE);
    private static final String END_DATE = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE);

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse create(ThemeRequest themeRequest) {
        validateDuplicated(themeRequest);
        Theme theme = new Theme(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
        Theme newTheme = themeRepository.save(theme);
        return new ThemeResponse(newTheme);
    }

    private void validateDuplicated(ThemeRequest themeRequest) {
        if (themeRepository.existsByName(themeRequest.name())) {
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
        List<Theme> themes = themeRepository.findByReservationTermAndCount(START_DATE, END_DATE, MAXIMUM_COUNT);
        return themes.stream().map(ThemeResponse::new).toList();
    }
}
