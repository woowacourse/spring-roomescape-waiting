package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByUserId(long id);

    List<Reservation> findByTimeSlotId(long id);

    List<Reservation> findByThemeId(long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, long themeId);

    Optional<Reservation> findByDateAndTimeSlotIdAndThemeId(LocalDate date, long timeSlotId, long themeId);
}
