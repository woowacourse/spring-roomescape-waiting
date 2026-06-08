package roomescape.repository;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWithWaitingOrder;

public interface LockedReservationWriter {

    ReservationWithWaitingOrder save(Reservation reservation);

    ReservationWithWaitingOrder updateAndRequeue(Reservation reservation);

    void cancel(Long id);

    boolean promoteEarliestWaiting(LocalDate date, Long timeId, Long themeId);
}
