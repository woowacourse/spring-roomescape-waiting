package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByName(String name);

    Optional<Reservation> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    Reservation update(Long id, LocalDate date, ReservationTime time);

    void delete(Long id);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);
}
