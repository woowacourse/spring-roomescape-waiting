package roomescape.infrastructure.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.WaitingDto;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.service.reader.WaitingReader;
import roomescape.infrastructure.repository.dao.JpaReservationSlotDao;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaWaitingReader implements WaitingReader {

    private final JpaReservationSlotDao slotDao;

    @Override
    public List<WaitingDto> getAll() {
        List<ReservationSlot> slots = slotDao.findAll();
        return slots.stream()
                .flatMap(slot -> slot.getWaitingReservations().stream())
                .map(WaitingDto::fromEntity)
                .toList();
    }
}
