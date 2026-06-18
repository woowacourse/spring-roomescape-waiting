package roomescape.date.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.date.domain.ReservationDate;

@Repository
public interface ReservationDateRepository extends JpaRepository<ReservationDate, Long> {

    List<ReservationDate> findAllByDateAfter(LocalDate date);

    boolean existsByDate(LocalDate date);
}
