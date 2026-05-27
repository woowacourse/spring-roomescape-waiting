package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservationWithoutId);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByName(String name);

    List<Reservation> findAll();

    void delete(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
