package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.exception.OperationNotAllowedCustomException;

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

    @Transactional(readOnly = true)
    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public ThemeResponse addTheme(ThemeRequest request) {
        Theme theme = request.toTheme();
        Theme savedTheme = themeRepository.save(theme);

        return ThemeResponse.from(savedTheme);
    }

    @Transactional
    public void deleteThemeById(Long id) {
        Theme theme = themeRepository.getThemeById(id);
        validateReservationNotExist(id);

        themeRepository.delete(theme);
    }

    private void validateReservationNotExist(Long id) {
        boolean exist = reservationRepository.existsByThemeId(id);
        if (exist) {
            throw new OperationNotAllowedCustomException("해당 테마에 예약이 존재하기 때문에 삭제할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getMostReservedThemes() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(ANALYSIS_PERIOD);
        List<Long> mostReservedThemesId = reservationRepository.findMostReservedThemesId(from, to);
        List<Theme> mostReserved = themeRepository.findAllById(mostReservedThemesId);

        return mostReserved.stream()
                .limit(ANALYSIS_COUNT_LIMIT)
                .map(ThemeResponse::from)
                .toList();
    }
}
