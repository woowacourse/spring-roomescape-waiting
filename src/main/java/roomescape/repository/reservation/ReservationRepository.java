package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@org.springframework.stereotype.Repository
public interface ReservationRepository extends Repository<Reservation, Long> {

    boolean existsByTimeId(Long id);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByDateBetween(LocalDate start, LocalDate end);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    boolean existsByThemeId(Long themeId);

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);
}
