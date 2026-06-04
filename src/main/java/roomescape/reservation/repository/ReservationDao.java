package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;

public interface ReservationDao {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAllByName(String name);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void update(Reservation reservation);

    void delete(Reservation reservation);

    boolean existsByDateAndTimeIdAndName(LocalDate date, Long timeId, String name);

    boolean existsByDateAndTimeIdAndNameAndIdNot(LocalDate date, Long timeId, String name, Long id);
}
