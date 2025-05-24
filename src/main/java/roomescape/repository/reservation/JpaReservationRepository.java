package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    boolean existsByTimeId(Long id);

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    List<Reservation> findAllByDateBetween(LocalDate dateBefore, LocalDate dateAfter);

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    boolean existsByThemeId(long themeId);
}
