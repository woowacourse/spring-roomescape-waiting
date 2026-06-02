package roomescape.date.repository;

import org.springframework.stereotype.Repository;
import roomescape.date.domain.ReservationDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationDateRepository {

    Optional<ReservationDate> findById(Long id);

    List<ReservationDate> findAll();

    List<ReservationDate> findSlotOfDatesByThemeId(Long themeId);

    List<ReservationDate> findAllAfterToday();

    ReservationDate save(ReservationDate reservationDate);

    boolean updateStatus(ReservationDate reservationDate);

    boolean existsByDate(LocalDate date);

}
