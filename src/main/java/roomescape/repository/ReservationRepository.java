package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import roomescape.domain.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findAll();

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    Optional<Reservation> findByTimeId(Long id);

    Optional<Reservation> findByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
