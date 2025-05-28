package roomescape.application;

import static roomescape.infrastructure.ReservationSpecs.byDateBetween;
import static roomescape.infrastructure.ReservationSpecs.byThemeId;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.Description;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.theme.Thumbnail;
import roomescape.exception.InUseException;

@Service
@AllArgsConstructor
public class ThemeService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public Theme register(final String name, final String description, final String thumbnail) {
        var theme = new Theme(new ThemeName(name), new Description(description), new Thumbnail(thumbnail));
        return themeRepository.save(theme);
    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    public void removeById(final long id) {
        if (reservationRepository.exists(byThemeId(id))) {
            throw new InUseException("삭제하려는 테마를 사용하는 예약이 있습니다.");
        }

        themeRepository.deleteByIdOrElseThrow(id);
    }

    public List<Theme> findPopularThemes(final LocalDate startDate, final LocalDate endDate, final int maxCount) {
        var reservations = reservationRepository.findAllWithWrapping(byDateBetween(startDate, endDate));
        return reservations.findPopularThemes(maxCount);
    }
}
