package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    void delete(long id);

    boolean existByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId);

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    void update(Reservation reservation);
}
