package roomescape.slot.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.infrastructure.SlotRepository;

@Component
@RequiredArgsConstructor
public class SlotUsageValidator {
    private final SlotRepository slotRepository;

    public void validateTimeDeletable(long timeId) {
        if (slotRepository.existsByTimeId(timeId)) {
            throw new EscapeRoomException(ErrorCode.SLOT_TIME_IN_USE, timeId);
        }
    }

    public void validateThemeDeletable(long themeId) {
        if (slotRepository.existsByThemeId(themeId)) {
            throw new EscapeRoomException(ErrorCode.SLOT_THEME_IN_USE, themeId);
        }
    }
}
