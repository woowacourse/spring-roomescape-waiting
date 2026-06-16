package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.domain.theme.FamousThemeCondition;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeJpaRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeService {
    private final ThemeJpaRepository themeJpaRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeJpaRepository themeJpaRepository, ThemeRepository themeRepository,
                        ReservationRepository reservationRepository) {
        this.themeJpaRepository = themeJpaRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Theme create(ThemeCreateRequest request) {
        Theme theme = Theme.create(new ThemeName(request.getName()), request.getDescription(),
                new ThumbnailUrl(request.getThumbnailUrl()));
        return themeJpaRepository.save(theme);
    }

    public Theme find(long themeId) {
        return themeJpaRepository.findById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.THEME_NOT_FOUND));
    }

    public List<Theme> findAll() {
        return themeJpaRepository.findAll();
    }

    public List<Theme> findFamous(ThemeFamousFindRequest request, LocalDate now) {
        FamousThemeCondition condition = new FamousThemeCondition(request.getRecentDays(), request.getBaseDate(),
                request.getLimit(), now);

        return themeRepository.findFamous(condition);
    }

    @Transactional
    public void delete(long themeId) {
        if (!themeJpaRepository.existsById(themeId)) {
            throw new RoomEscapeException(ErrorCode.THEME_NOT_FOUND);
        }

        if (reservationRepository.existsByThemeId(themeId)) {
            throw new RoomEscapeException(ErrorCode.THEME_IN_USE);
        }

        themeJpaRepository.deleteById(themeId);
    }
}
