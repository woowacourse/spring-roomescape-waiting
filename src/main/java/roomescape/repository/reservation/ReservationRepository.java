package roomescape.repository.reservation;

import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Optional<Reservation> findBySlot(ReservationSlot slot);

    boolean existsByTime(ReservationTime time);

    Reservation save(Reservation reservation);

    void delete(Reservation reservation);
}
