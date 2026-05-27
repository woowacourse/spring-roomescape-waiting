package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    Reservation create(Reservation reservationWithoutId);

    Optional<Reservation> readById(Long id);

    Optional<Reservation> readBySlot(LocalDate date, Long timeId, Long themeId);

    List<Reservation> readByName(String name);

    List<Reservation> readAll();

    void delete(Long id);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);
}
