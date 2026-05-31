package roomescape.slot.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.Slot;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.slot.dto.request.SlotSaveRequest;
import roomescape.slot.dto.response.SlotFindResponse;
import roomescape.slot.dto.response.SlotSaveResponse;
import roomescape.slot.infrastructure.SlotRepository;
import roomescape.theme.infrastructure.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public long resolveSlotId(LocalDate date, long timeId, long themeId) {
        return getSlotIdOrThrow(date, timeId, themeId);
    }

    public List<SlotFindResponse> findAll() {
        List<Slot> slots = slotRepository.findAll();
        return SlotFindResponse.from(slots);
    }

    public SlotFindResponse findById(long id) {
        Slot slot = getSlotOrElseThrow(id);
        return SlotFindResponse.from(slot);
    }

    public SlotSaveResponse save(SlotSaveRequest body) {
        validateSlot(body.date(), body.timeId(), body.themeId());
        throwIfSlotAlreadyExists(body.date(), body.themeId(), body.timeId());
        return SlotSaveResponse.from(slotRepository.save(body.toDomain()));
    }

    public void deleteById(long slotId) {
        slotRepository.deleteById(slotId);
    }

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

    public void validateSlot(LocalDate date, Long timeId, Long themeId) {
        validateNotPastDate(date);
        ReservationTime reservationTime = getReservationTimeOrThrow(timeId);
        validateNotPastTime(date, reservationTime.startAt());
        getThemeOrThrow(themeId);
    }

    public void validateNotPastDate(LocalDate date) {
        if (date.isBefore(LocalDate.now(clock))) {
            throw new EscapeRoomException(ErrorCode.PAST_SLOT);
        }
    }

    public void validateNotPastTime(LocalDate date, LocalTime time) {
        LocalDate currentDate = LocalDate.now(clock);
        LocalTime currentTime = LocalTime.now(clock);

        if (date.isEqual(currentDate) && time.isBefore(currentTime)) {
            throw new EscapeRoomException(ErrorCode.PAST_SLOT);
        }
    }

    private void throwIfSlotAlreadyExists(LocalDate date, long themeId, long timeId) {
        if (slotRepository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId)) {
            throw new EscapeRoomException(ErrorCode.SLOT_ALREADY_EXIST);
        }
    }

    private void getThemeOrThrow(Long themeId) {
        themeRepository.findById(themeId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.THEME_NOT_FOUND, themeId));
    }

    private ReservationTime getReservationTimeOrThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATIONTIME_NOT_FOUND, timeId));
    }

    private long getSlotIdOrThrow(LocalDate date, long timeId, long themeId) {
        return slotRepository.findSlotIdByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.SLOT_NOT_FOUND_WITH_CONDITION, date, timeId, themeId));
    }

    private Slot getSlotOrElseThrow(long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.SLOT_NOT_FOUND, slotId));
    }
}
