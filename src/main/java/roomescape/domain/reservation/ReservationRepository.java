package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByUserId(long id);

    List<Reservation> findByDateAndThemeId(LocalDate date, long themeId);

    Optional<Reservation> findByDateAndTimeSlotIdAndThemeId(LocalDate date, long timeSlotId, long themeId);

    boolean existsByTimeSlotId(long timeSlotId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndTimeSlotIdAndThemeIdAndUserId(LocalDate date, long timeSlotId, long themeId, long userId);
}
