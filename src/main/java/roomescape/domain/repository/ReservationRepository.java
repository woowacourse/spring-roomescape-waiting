package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public interface ReservationRepository extends Repository<Reservation, Long> {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void delete(Reservation reservation);

    void deleteAll();
}
