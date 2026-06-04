package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAllByName(String name);

    Optional<Reservation> findById(long id);

    Optional<Reservation> findBySlot(ReservationSlot slot);

    void delete(Reservation reservation);

    boolean hasBookingAtSameTime(Reservation reservation);

    boolean isAlreadyBookedByOthers(Reservation reservation);
}
