package roomescape.repository;

import roomescape.domain.Reservation;
import roomescape.domain.Reservations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll(int offset, int limit);

    long count();

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    void transferWithPendingStatus(Long id, String name);

    void deleteById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    Optional<Reservation> findById(Long id);

    Reservations findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByName(String name, int offset, int limit);

    long countByName(String name);
}
