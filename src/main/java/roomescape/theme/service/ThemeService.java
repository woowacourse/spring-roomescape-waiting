package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeCreateRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    public static final int POPULAR_THEME_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(final ThemeCreateRequest requestDto) {
        if (themeRepository.existsByName(requestDto.name())) {
            throw new ConflictException(ExceptionCause.THEME_NAME_DUPLICATE);
        }
        Theme requestTheme = requestDto.createWithoutId();
        Theme savedTheme = themeRepository.save(requestTheme);
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> allTheme = themeRepository.findAll();
        return allTheme.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteThemeById(final Long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new NotFoundException(ExceptionCause.THEME_NOTFOUND);
        }
        if (reservationRepository.findByThemeId(id).isPresent()) {
            throw new BadRequestException(ExceptionCause.THEME_EXIST);
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> findPopularThemes() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);

        List<Theme> themes = themeRepository.findTopThemes(start, end, POPULAR_THEME_LIMIT);
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
