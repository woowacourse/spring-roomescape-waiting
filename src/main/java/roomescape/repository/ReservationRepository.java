package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    Reservation create(Reservation reservationWithoutId);

    Optional<Reservation> readById(Long id);

    List<Reservation> readByName(String name);

    List<Reservation> readAll();

    void update(Long id, LocalDate date, Long timeId);

    void delete(Long id);

    boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);
}
