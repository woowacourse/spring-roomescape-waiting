package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    Reservation add(Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(long addedReservationId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByDateInRange(LocalDate start, LocalDate end);

    List<Reservation> findAllByFilter(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(long id);

    boolean existsByDateAndTimeIdAndTheme(Reservation reservation);

    void deleteById(Long id);
}
