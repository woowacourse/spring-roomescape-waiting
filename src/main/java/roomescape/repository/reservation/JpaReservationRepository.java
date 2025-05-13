package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(Long id);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByDateBetween(LocalDate dateAfter, LocalDate dateBefore);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    @Query("select count(r) > 0 from Reservation r where r.theme.id = :#{#themeId}")
    boolean existsByThemeId(long themeId);
}
