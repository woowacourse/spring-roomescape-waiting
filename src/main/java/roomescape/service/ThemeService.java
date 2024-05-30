package roomescape.service;

import static roomescape.exception.ExceptionType.DELETE_USED_THEME;
import static roomescape.exception.ExceptionType.DUPLICATE_THEME;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Duration;
import roomescape.domain.Theme;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository,
                        ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse save(ThemeRequest themeRequest) {
        if (themeRepository.existsByName(themeRequest.name())) {
            throw new RoomescapeException(DUPLICATE_THEME);
        }
        Theme beforeSavedTheme = themeRequest.toTheme();
        Theme savedTheme = themeRepository.save(beforeSavedTheme);
        return ThemeResponse.from(savedTheme);
    }

    public List<ThemeResponse> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findAndOrderByPopularity(int count) {
        Duration lastWeek = Duration.ofLastWeek();
        return reservationRepository.findAndOrderByPopularity(lastWeek.getStartDate(), lastWeek.getEndDate(), count)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public void delete(long themeId) {
        if (isUsedTheme(themeId)) {
            throw new RoomescapeException(DELETE_USED_THEME);
        }
        themeRepository.deleteById(themeId);
    }

    private boolean isUsedTheme(long themeId) {
        return reservationRepository.existsByThemeId(themeId);
    }
}
