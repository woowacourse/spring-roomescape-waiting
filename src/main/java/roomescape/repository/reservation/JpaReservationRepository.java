package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @Transactional(readOnly = true)
    boolean existsByTimeId(Long id);

    @Query("""
        select r from Reservation r
        join fetch r.time
        join fetch r.theme
        where r.date = :date and r.theme.id = :themeId
    """)
    @Transactional(readOnly = true)
    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
        select r from Reservation r
        join fetch r.time
        join fetch r.theme
        where r.date between :dateBefore and :dateAfter
    """)
    @Transactional(readOnly = true)
    List<Reservation> findAllByDateBetween(LocalDate dateBefore, LocalDate dateAfter);

    @Transactional(readOnly = true)
    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    @Transactional(readOnly = true)
    boolean existsByThemeId(long themeId);
}
