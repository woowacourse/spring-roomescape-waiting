package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedThemeException;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(ThemeRequest themeRequest) {
        Optional<Theme> optionalTheme = themeRepository.findByName(themeRequest.name());
        if (optionalTheme.isPresent()) {
            throw new ExistedThemeException();
        }

        Theme theme = Theme.createWithoutId(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
        Theme themeWithId = themeRepository.save(theme);
        return new ThemeResponse(themeWithId.getId(), themeWithId.getName(), themeWithId.getDescription(),
                themeWithId.getThumbnail());
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(theme ->
                        new ThemeResponse(
                                theme.getId(),
                                theme.getName(),
                                theme.getDescription(),
                                theme.getThumbnail()
                        )
                )
                .toList();
    }

    public int deleteThemeById(long id) {
        List<Reservation> reservations = reservationRepository.findByThemeId(id);
        if (reservations.size() > 0) {
            throw new ExistedReservationException();
        }
        return themeRepository.deleteById(id);
    }

    public List<ThemeResponse> getTop10MostReservedThemesInLast7Days() {
        LocalDate startDate = LocalDate.now().minusDays(8);
        List<Theme> themes = themeRepository.findByDateRangeOrderByReservationCountLimitN(startDate,
                LocalDate.now().minusDays(1), 10);

        return themes.stream().map(theme -> new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(),
                theme.getThumbnail())).toList();
    }
}
