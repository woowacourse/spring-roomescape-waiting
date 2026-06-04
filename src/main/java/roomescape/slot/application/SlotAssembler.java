package roomescape.slot.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.slot.Slot;
import roomescape.slot.infrastructure.SlotRepository;
import roomescape.theme.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@Component
@RequiredArgsConstructor
public class SlotAssembler {
    private final SlotRepository slotRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public Slot assembleExisting(LocalDate date, long timeId, long themeId) {
        Slot slot = slotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.SLOT_NOT_FOUND_WITH_CONDITION,
                        date,
                        timeId,
                        themeId));
        slot.validateNotPast(LocalDateTime.now(clock));
        return slot;
    }

    public Slot assembleNew(LocalDate date, long timeId, long themeId) {
        ReservationTime reservationTime = getReservationTimeOrThrow(timeId);
        Theme theme = getThemeOrThrow(themeId);
        Slot slot = Slot.create(date, reservationTime, theme);
        slot.validateNotPast(LocalDateTime.now(clock));
        return slot;
    }

    private Theme getThemeOrThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.THEME_NOT_FOUND, themeId));
    }

    private ReservationTime getReservationTimeOrThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATIONTIME_NOT_FOUND, timeId));
    }
}
