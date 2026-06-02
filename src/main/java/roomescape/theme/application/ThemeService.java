package roomescape.theme.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.application.SlotService;
import roomescape.theme.Theme;
import roomescape.theme.dto.request.ThemeSaveRequest;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.theme.dto.response.ThemeSaveResponse;
import roomescape.theme.infrastructure.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final SlotService slotService;
    private final Clock clock;

    public ThemeSaveResponse save(ThemeSaveRequest body) {
        validateAlreadyThemeNot(body.name());
        return ThemeSaveResponse.from(themeRepository.save(body.toDomain()));
    }

    public void delete(long id) {
        slotService.validateThemeDeletable(id);
        themeRepository.deleteById(id);
    }

    public List<ThemeFindResponse> findThemesBySlotDate(LocalDate date) {
        List<Theme> themes = themeRepository.findThemesBySlotDate(date);
        return ThemeFindResponse.from(themes);
    }

    public List<ThemeFindResponse> findPopularTheme() {
        LocalDate today = LocalDate.now(clock);
        List<Theme> themes = themeRepository.findPopularThemeByCurrentDate(today);
        return ThemeFindResponse.from(themes);
    }

    public List<ThemeFindResponse> findAll() {
        List<Theme> themes = themeRepository.findAll();
        return ThemeFindResponse.from(themes);
    }

    private void validateAlreadyThemeNot(String themeName) {
        if (themeRepository.existsAlreadyTheme(themeName)) {
            throw new EscapeRoomException(ErrorCode.THEME_ALREADY_EXIST);
        }
    }
}
