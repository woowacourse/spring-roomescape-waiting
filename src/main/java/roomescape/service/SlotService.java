package roomescape.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.SlotDao;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@Service
@Transactional(readOnly = true)
public class SlotService {

    private final SlotDao slotDao;

    public SlotService(SlotDao slotDao) {
        this.slotDao = slotDao;
    }

    @Transactional
    public Slot findOrCreate(LocalDate date, ReservationTime time, Theme theme) {
        return slotDao.findOrCreate(new Slot(date, time, theme));
    }
}
