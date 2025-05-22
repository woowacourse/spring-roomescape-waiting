package roomescape.business.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.repository.ReservationSlots;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationSlotReader {

    private final ReservationSlots slots;

    public List<ReservationSlot> getAllSlotsContainsReserverOf(final String userIdValue) {
        return slots.findAllSlotsContainsReserverOf(Id.create(userIdValue));
    }

    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(final LocalDate date, final String timeIdValue, final String themeIdValue) {
        return slots.findByDateAndTimeIdAndThemeId(date, Id.create(timeIdValue), Id.create(themeIdValue));
    }
}
