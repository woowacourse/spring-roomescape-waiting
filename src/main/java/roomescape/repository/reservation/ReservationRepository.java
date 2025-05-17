package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository {

    long save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);

    boolean existsByTimeId(Long id);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    Optional<Reservation> findById(long addedReservationId);

    List<Reservation> findAllByDateInRange(LocalDate start, LocalDate end);

    boolean existsByDateAndTimeAndTheme(Reservation reservation);

    boolean existsByThemeId(long id);
}
