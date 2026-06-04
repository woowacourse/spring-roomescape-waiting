package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByIdAndName(long id, String name);

    int deleteById(long id);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);
}
