package roomescape.slot.application.port.in;

import java.util.List;
import roomescape.slot.application.dto.response.SlotFindResponse;

public interface FindSlotUseCase {
    List<SlotFindResponse> findAll();
    SlotFindResponse findById(long id);
}
