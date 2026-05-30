package roomescape.infrastructure;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.slot.Slot;

@Component
public class SlotManager {

    private final ConcurrentHashMap<Slot, Boolean> confirmedSlots = new ConcurrentHashMap<>();
    private final ReservationDao reservationDao;
    private final Clock clock;

    public SlotManager(ReservationDao reservationDao, Clock clock) {
        this.reservationDao = reservationDao;
        this.clock = clock;
    }

    @PostConstruct
    public void init() {
        List<Reservation> confirmedReservations = reservationDao.findByAfterDateTime(LocalDateTime.now(clock));
        for (Reservation reservation : confirmedReservations) {
            confirmedSlots.put(
                    Slot.from(
                            reservation.getDate(),
                            reservation.getTime(),
                            reservation.getTheme()
                    ),
                    Boolean.TRUE);
        }
    }

    public boolean tryAcquire(Slot slot) {
        return confirmedSlots.putIfAbsent(slot, Boolean.TRUE) == null;
    }

    public boolean tryChange(Slot originSlot, Slot modifiedSlot) {
        if (confirmedSlots.putIfAbsent(modifiedSlot, Boolean.TRUE) == null) {
            confirmedSlots.remove(originSlot);
            return true;
        }

        return false;
    }

    public void release(Slot slot) {
        confirmedSlots.remove(slot);
    }
}
