package roomescape.service;

import static roomescape.exception.RoomescapeExceptionCode.CANNOT_DELETE_THEME_REFERENCED_BY_RESERVATION;
import static roomescape.exception.RoomescapeExceptionCode.THEME_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

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

    public List<ThemeResponse> findThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse createTheme(ThemeRequest request) {
        Theme theme = request.toTheme();
        Theme savedTheme = themeRepository.save(theme);

        return ThemeResponse.from(savedTheme);
    }

    public void deleteThemeById(Long id) {
        boolean exist = reservationRepository.existsByThemeId(id);
        if (exist) {
            throw new RoomescapeException(CANNOT_DELETE_THEME_REFERENCED_BY_RESERVATION);
        }
        themeRepository.delete(getTheme(id));
    }

    public List<ThemeResponse> findMostReservedThemes() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(DAYS_IN_WEEK);

        List<Reservation> mostReserved = reservationRepository.findByDateBetweenOrderByThemeCountDesc(from, to);

        return mostReserved.stream()
                .limit(RANKING_COUNT_LIMIT)
                .map(Reservation::getTheme)
                .map(ThemeResponse::from)
                .toList();
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(THEME_NOT_FOUND));
    }
}
