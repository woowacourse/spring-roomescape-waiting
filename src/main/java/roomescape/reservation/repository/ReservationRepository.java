package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();

    List<Reservation> findByName(String name);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    boolean existsConflict(String name, ReservationSlot slot);

    boolean existsConflictExcluding(String name, ReservationSlot slot, Long id);

    void deleteById(Long id);
}
