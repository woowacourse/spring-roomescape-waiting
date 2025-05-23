package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.dto.business.ThemeCreationContent;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.local.AlreadyReservedThemeException;
import roomescape.exception.local.AlreadyWaitingThemeException;
import roomescape.exception.local.NotFoundThemeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ThemeService(
            ThemeRepository themeRepository,
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ThemeResponse> findAllThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> findTopThemes(LocalDate from, LocalDate to, int size) {
        List<Theme> themes = themeRepository.findThemesOrderByReservationCount(from, to, size);
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public ThemeResponse addTheme(ThemeCreationContent request) {
        Theme theme = Theme.createWithoutId(request.name(), request.description(), request.thumbnail());
        Theme savedTheme = themeRepository.save(theme);
        return new ThemeResponse(savedTheme);
    }

    public void deleteThemeById(Long id) {
        Theme theme = getThemeById(id);
        validateReservationInTheme(theme);
        validateMemberInTime(theme);
        themeRepository.deleteById(id);
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(NotFoundThemeException::new);
    }

    private void validateReservationInTheme(Theme theme) {
        boolean isExistReservation = reservationRepository.existsByTheme(theme);
        if (isExistReservation) {
            throw new AlreadyReservedThemeException();
        }
    }

    private void validateMemberInTime(Theme theme) {
        boolean isExistWaiting = waitingRepository.existsByTheme(theme);
        if (isExistWaiting) {
            throw new AlreadyWaitingThemeException();
        }
    }
}
