package roomescape.domain.reservationWaiting;

import java.util.List;
import java.util.Optional;

public interface ReservationWaitingRepository {

    boolean isExistByNameAndSlotId(String name, Long slotId);

    Optional<ReservationWaiting> findFirstBySlotId(Long slotId);

    Optional<ReservationWaiting> findReservationWaitingById(long id);

    List<ReservationWaiting> findAllReservationWaiting();

    List<ReservationWaiting> findAllByName(String name);

    Long create(ReservationWaiting reservationWaiting);

    long delete(Long id);
}
