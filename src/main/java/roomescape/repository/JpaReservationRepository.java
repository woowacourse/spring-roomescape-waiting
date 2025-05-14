package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(Long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByDateTimeAndThemeId(LocalDate date, LocalTime time, long themeId);

    //TODO
    List<Reservation> findReservationsByDateBetweenAndThemeIdAndMemberId(long themeId, long memberId, LocalDate dateBefore, LocalDate dateAfter);

    boolean existsByTimeId(Long id);
}
