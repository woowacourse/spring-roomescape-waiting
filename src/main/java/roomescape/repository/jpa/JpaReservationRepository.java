package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.entity.Reservation;
import roomescape.repository.ReservationRepository;

public interface JpaReservationRepository extends ReservationRepository,CrudRepository<Reservation,Long> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
