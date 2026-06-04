package roomescape.repository.reservation;

import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Optional<Reservation> findBySlot(ReservationSlot slot);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);
}
