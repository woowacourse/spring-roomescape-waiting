package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.model.Reservation;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> searchBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsById(Long id);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    void deleteById(Long id);
}
