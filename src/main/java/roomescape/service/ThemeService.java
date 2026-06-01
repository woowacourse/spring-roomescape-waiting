package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
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
        return themeRepository.findById(themeId).orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다. 입력을 확인해 주세요."));
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
            throw new NotFoundException("존재하지 않는 테마입니다. 입력을 확인해 주세요.");
        }

        if (reservationRepository.existsByThemeId(themeId)) {
            throw new ConflictException("테마를 사용하는 예약이 존재합니다. 관련 예약을 지우고 요청해 주세요");
        }

        themeRepository.deleteById(themeId);
    }
}
