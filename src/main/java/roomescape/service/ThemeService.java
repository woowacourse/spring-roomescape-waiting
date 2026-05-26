package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeService {
    private static final long DEFAULT_DAYS = 7;
    private static final long DEFAULT_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Theme create(ThemeCreateRequest request) {
        Theme theme = Theme.create(new ThemeName(request.getName()), request.getDescription(),
                new ThumbnailUrl(request.getThumbnailUrl()));
        return themeRepository.save(theme);
    }

    public Theme find(long themeId) {
        return themeRepository.findById(themeId).orElseThrow(() -> new RoomEscapeException(ErrorCode.THEME_NOT_FOUND));
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findFamous(ThemeFamousFindRequest request, LocalDate now) {
        Long days = request.getDays();
        LocalDate date = request.getDate();
        Long limit = request.getLimit();

        if (days == null) {
            days = DEFAULT_DAYS;
        }
        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (date == null) {
            date = now;
        }
        return themeRepository.findFamous(days, date, limit);
    }

    @Transactional
    public void delete(long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new RoomEscapeException(ErrorCode.THEME_NOT_FOUND);
        }

        if (reservationRepository.existsByThemeId(themeId)) {
            throw new RoomEscapeException(ErrorCode.THEME_IN_USE);
        }

        themeRepository.deleteById(themeId);
    }
}
