package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.exception.OperationNotAllowedException;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {

    public static final int ANALYSIS_PERIOD = 7;
    public static final int ANALYSIS_COUNT_LIMIT = 10;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse addTheme(ThemeRequest request) {
        Theme theme = request.toTheme();
        Theme savedTheme = themeRepository.save(theme);

        return ThemeResponse.from(savedTheme);
    }

    public void deleteThemeById(Long id) {
        findValidatedTheme(id);
        validateReservationNotExist(id);

        themeRepository.deleteById(id);
    }

    private Theme findValidatedTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 테마를 찾을 수 없습니다."));
    }

    private void validateReservationNotExist(Long id) {
        boolean exist = reservationRepository.existsByThemeId(id);
        if (exist) {
            throw new OperationNotAllowedException("해당 테마에 예약이 존재하기 때문에 삭제할 수 없습니다.");
        }
    }

    public List<ThemeResponse> getMostReservedThemes() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(ANALYSIS_PERIOD);
        List<Reservation> mostReserved = reservationRepository.findMostReserved(from, to);

        return mostReserved.stream()
                .limit(ANALYSIS_COUNT_LIMIT)
                .map(Reservation::getTheme)
                .map(ThemeResponse::from)
                .toList();
    }
}
