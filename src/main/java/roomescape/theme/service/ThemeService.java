package roomescape.theme.service;

import static roomescape.theme.exception.ThemeErrorCode.THEME_HAS_RESERVATION;
import static roomescape.theme.exception.ThemeErrorCode.THEME_NOT_FOUND;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DomainException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Transactional
    public Theme create(String name, String description, String thumbnail) {
        Theme theme = Theme.create(name, description, thumbnail);

        return themeRepository.save(theme);
    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    public List<Theme> findPopularThemes(int days, int size) {
        LocalDate now = LocalDate.now(clock);
        LocalDate startDate = now.minusDays(days);
        LocalDate endDate = now.minusDays(1);

        return themeRepository.findTopThemesByReservationCount(startDate, endDate, size);
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existByThemeId(id)) {
            throw new DomainException(THEME_HAS_RESERVATION);
        }

        if (!themeRepository.cancelById(id, LocalDateTime.now(clock))) {
            throw new DomainException(THEME_NOT_FOUND);
        }
    }
}
