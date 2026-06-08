package roomescape.infrastructure;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.slot.EventSlot;

@Component
public class SlotManager {

    private final ConcurrentHashMap<EventSlot, Boolean> confirmedSlots = new ConcurrentHashMap<>();
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
                    reservation.getEventSlot(),
                    Boolean.TRUE
            );
        }
    }

    public boolean tryAcquire(EventSlot eventSlot) {
        return confirmedSlots.putIfAbsent(eventSlot, Boolean.TRUE) == null;
    }

    public void release(EventSlot eventSlot) {
        confirmedSlots.remove(eventSlot);
    }
}
