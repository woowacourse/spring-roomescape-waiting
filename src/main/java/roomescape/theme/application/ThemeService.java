package roomescape.theme.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.application.SlotUsageValidator;
import roomescape.theme.application.dto.request.ThemeSaveRequest;
import roomescape.theme.application.dto.response.ThemeFindResponse;
import roomescape.theme.application.dto.response.ThemeSaveResponse;
import roomescape.theme.application.port.in.CreateThemeUseCase;
import roomescape.theme.application.port.in.DeleteThemeUseCase;
import roomescape.theme.application.port.in.FindThemeUseCase;
import roomescape.theme.application.port.out.ThemeRepository;
import roomescape.theme.domain.Theme;

@Service
@RequiredArgsConstructor
public class ThemeService implements CreateThemeUseCase, FindThemeUseCase, DeleteThemeUseCase {
    private final ThemeRepository themeRepository;
    private final ThemeAssembler themeAssembler;
    private final SlotUsageValidator slotUsageValidator;
    private final Clock clock;

    public ThemeSaveResponse save(ThemeSaveRequest body) {
        validateAlreadyThemeNot(body.name());
        Theme theme = themeAssembler.assemble(body.name(), body.description(), body.thumbnailUrl(), body.price());
        return ThemeSaveResponse.from(themeRepository.save(theme));
    }

    private void validateAlreadyThemeNot(String themeName) {
        if (themeRepository.existsAlreadyTheme(themeName)) {
            throw new EscapeRoomException(ErrorCode.THEME_ALREADY_EXIST);
        }
    }

    public void delete(long id) {
        slotUsageValidator.validateThemeDeletable(id);
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

}
