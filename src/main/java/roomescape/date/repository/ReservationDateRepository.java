package roomescape.date.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.date.domain.ReservationDate;

@Repository
public interface ReservationDateRepository {

    Optional<ReservationDate> findById(Long id);

    List<ReservationDate> findAll();

    List<ReservationDate> findAllAfterToday();

    ReservationDate save(ReservationDate reservationDate);

    boolean updateStatus(ReservationDate reservationDate);

    boolean existsByDate(LocalDate date);

}
