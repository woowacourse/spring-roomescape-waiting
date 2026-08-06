package roomescape.domain.reservationdate;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationDateRepository extends JpaRepository<ReservationDate, Long> {

    boolean existsByPlayDay(LocalDate playDay);
}
