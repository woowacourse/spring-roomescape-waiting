package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ThemeCreateRequest;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.domain.theme.FamousThemeCondition;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final SlotRepository slotRepository;

    @Transactional
    public Theme create(ThemeCreateRequest request) {
        Theme theme = Theme.create(new ThemeName(request.getName()), request.getDescription(),
                new ThumbnailUrl(request.getThumbnailUrl()));
        return themeRepository.save(theme);
    }

    public Theme find(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.THEME_NOT_FOUND));
    }

    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    public List<Theme> findFamous(ThemeFamousFindRequest request, LocalDate now) {
        FamousThemeCondition condition = new FamousThemeCondition(request.getRecentDays(), request.getBaseDate(),
                request.getLimit(), now);

        Pageable pageable = PageRequest.of(0, condition.getLimit().intValue());
        return themeRepository.findFamous(condition.startDate(), condition.endDate(), pageable);
    }

    @Transactional
    public void delete(long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.THEME_NOT_FOUND));

        slotRepository.findByTheme(theme).ifPresent(slot -> {
            throw new RoomEscapeException(ErrorCode.THEME_IN_USE);
        });

        themeRepository.deleteById(themeId);
    }
}
