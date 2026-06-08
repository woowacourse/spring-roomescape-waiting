package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;
import roomescape.domain.reservation.SlotRepository;
import roomescape.domain.theme.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeService {
    private static final long DEFAULT_DAYS = 7;
    private static final long DEFAULT_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final SlotRepository slotRepository;

    public ThemeService(
            ThemeRepository themeRepository,
            SlotRepository slotRepository
    ) {
        this.themeRepository = themeRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public Theme create(ThemeCreateRequest request) {
        Theme theme = Theme.create(
                new ThemeName(request.getName()),
                request.getDescription(),
                new ThumbnailUrl(request.getThumbnailUrl())
        );

        return themeRepository.save(theme);
    }

    public Theme find(long themeId) {
        return themeRepository.getById(themeId);
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findFamous(int limit, int days, LocalDate date) {
        return themeRepository.findFamous(days, date, limit);
    }

    @Transactional
    public void delete(long themeId) {
        if (!themeRepository.existsById(themeId)) {
            throw new NotFoundException("존재하지 않는 테마입니다. 입력을 확인해 주세요.");
        }

        if (slotRepository.existsByThemeId(themeId)) {
            throw new ConflictException("테마를 사용하는 예약이 존재합니다. 관련 예약을 지우고 요청해 주세요");
        }

        themeRepository.deleteById(themeId);
    }
}
