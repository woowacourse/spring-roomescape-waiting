package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository extends Repository<Reservation, Long> {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    Optional<Reservation> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    void delete(Reservation reservation);

    void deleteAll();
}
