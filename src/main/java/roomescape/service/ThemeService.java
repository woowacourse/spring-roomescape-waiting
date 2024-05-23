package roomescape.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.service.exception.OperationNotAllowedException;
import roomescape.service.exception.ResourceNotFoundException;

@Service
public class ThemeService {

    private static final int DAYS_IN_WEEK = 7;
    private static final int RANKING_COUNT_LIMIT = 10;

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
        boolean exist = reservationRepository.existsByThemeId(id);
        if (exist) {
            throw new OperationNotAllowedException("해당 테마에 예약이 존재하기 때문에 삭제할 수 없습니다.");
        }
        themeRepository.delete(findValidatedTheme(id));
    }

    public List<ThemeResponse> getMostReservedThemes() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(DAYS_IN_WEEK);

        List<Reservation> mostReserved = reservationRepository.findByDateBetweenOrderByThemeCountDesc(from, to);

        return mostReserved.stream()
                .limit(RANKING_COUNT_LIMIT)
                .map(Reservation::getTheme)
                .map(ThemeResponse::from)
                .toList();
    }

    private Theme findValidatedTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("아이디에 해당하는 테마를 찾을 수 없습니다."));
    }
}
