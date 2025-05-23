package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(Long id);

    List<Reservation> findReservationsByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findReservationsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findReservationsByDateBetweenAndThemeIdAndMemberId(LocalDate dateBefore, LocalDate dateAfter,
                                                                         long themeId, long memberId);

    List<Reservation> findReservationsByMemberId(long id);

    List<Reservation> findReservationsByStatus(ReservationStatus status);
}
