package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
    Optional<Reservation> findByIdForUpdate(Long id);
    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
    Optional<Reservation> findByDateAndTimeIdAndThemeIdForUpdate(LocalDate date, Long timeId, Long themeId);
    List<Reservation> findAll();
    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);
    List<Reservation> findByName(String name);
    boolean existsByReservationTimeId(Long reservationId);
    boolean existsByThemeId(Long themeId);
    void updateSchedule(Reservation targetReservation);
    boolean deleteById(Long id);
    boolean deleteByIdAndName(Long id, String name);
}
