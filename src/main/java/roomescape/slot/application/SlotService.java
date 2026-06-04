package roomescape.slot.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.Slot;
import roomescape.slot.dto.request.SlotSaveRequest;
import roomescape.slot.dto.response.SlotFindResponse;
import roomescape.slot.dto.response.SlotSaveResponse;
import roomescape.slot.infrastructure.SlotRepository;

@Service
@RequiredArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final SlotAssembler slotAssembler;

    public List<SlotFindResponse> findAll() {
        List<Slot> slots = slotRepository.findAll();
        return SlotFindResponse.from(slots);
    }

    public SlotFindResponse findById(long id) {
        Slot slot = getSlotOrElseThrow(id);
        return SlotFindResponse.from(slot);
    }

    public SlotSaveResponse save(SlotSaveRequest body) {
        Slot slot = slotAssembler.assembleNew(body.date(), body.timeId(), body.themeId());
        throwIfSlotAlreadyExists(body.date(), body.themeId(), body.timeId());
        return SlotSaveResponse.from(slotRepository.save(slot));
    }

    public void deleteById(long slotId) {
        slotRepository.deleteById(slotId);
    }

    private void throwIfSlotAlreadyExists(LocalDate date, long themeId, long timeId) {
        if (slotRepository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId)) {
            throw new EscapeRoomException(ErrorCode.SLOT_ALREADY_EXIST);
        }
    }

    private Slot getSlotOrElseThrow(long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.SLOT_NOT_FOUND, slotId));
    }
}
