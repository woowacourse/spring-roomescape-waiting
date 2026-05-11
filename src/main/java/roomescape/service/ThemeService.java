package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorCode;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Transactional
    public Theme create(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);

        return themeRepository.save(theme);
    }

    @Transactional(readOnly = true)
    public List<Theme> findAllThemes(){
        return themeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Theme> findPopularThemes(int days, int size){
        LocalDate now = LocalDate.now(clock);
        LocalDate startDate = now.minusDays(days);
        LocalDate endDate = now.minusDays(1);

        return themeRepository.findTopThemesByReservationCount(startDate, endDate, size);
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existByThemeId(id)) {
            throw new DomainException(ErrorCode.THEME_HAS_RESERVATION);
        }

        if (!themeRepository.deleteById(id)) {
            throw new DomainException(ErrorCode.THEME_NOT_FOUND);
        }
    }
}
