package roomescape.business.application_service.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.WaitingDto;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.repository.ReservationSlots;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingReader {

    private final ReservationSlots slots;

    public List<WaitingDto> getAll() {
        List<ReservationSlot> slots = this.slots.findAll();
        return WaitingDto.fromEntities(slots);
    }
}
