package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Optional<Reservation> findBySlot(ReservationSlot slot);

    List<Reservation> findByName(ReservationName name);

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    boolean existsByTime(ReservationTime time);

    Reservation save(Reservation reservation);

    void delete(Reservation reservation);
}
