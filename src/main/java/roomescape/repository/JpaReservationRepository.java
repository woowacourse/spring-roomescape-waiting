package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findReservationsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findReservationsByDateBetweenAndThemeIdAndMemberId(LocalDate dateBefore, LocalDate dateAfter, long themeId, long memberId);

    boolean existsByTimeId(Long id);

    List<Reservation> findReservationsByMemberId(long id);
}
