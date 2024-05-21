package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationStatusRepository {

    ReservationStatus save(ReservationStatus reservationStatus);

    ReservationStatus getById(long id);

    Optional<ReservationStatus> findFirstWaiting(Reservation reservation);

    long getWaitingCount(Reservation reservation);

    List<ReservationStatus> findActiveReservationStatusesByMemberId(long memberId);

    boolean existsAlreadyWaitingOrBooked(Reservation reservation);

    List<Reservation> findAllBookedReservations();

    List<Reservation> findAllWaitingReservations();
}
