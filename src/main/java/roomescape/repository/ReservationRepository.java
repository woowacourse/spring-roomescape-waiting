package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;

@Repository
public interface ReservationRepository {

    List<Reservation> findAll();

    List<Long> findByThemeIdAndDate(long themeId, LocalDate date);

    Optional<Reservation> findById(long id);

    List<Reservation> findByName(String name);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    int update(Reservation reservation);
}
