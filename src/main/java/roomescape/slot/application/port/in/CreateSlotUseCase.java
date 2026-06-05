package roomescape.slot.application.port.in;

import roomescape.slot.application.dto.request.SlotSaveRequest;
import roomescape.slot.application.dto.response.SlotSaveResponse;

public interface CreateSlotUseCase {
    SlotSaveResponse save(SlotSaveRequest body);
}
