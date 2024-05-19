package roomescape.time.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

@Repository
public interface TimeRepository extends ListCrudRepository<ReservationTime, Long> {

    @Query("""
            SELECT t FROM Reservation AS r
            INNER JOIN r.time AS t
            WHERE r.date = :date
            AND r.theme.id = :themeId
            """)
    List<ReservationTime> findTimesExistsReservationDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByStartAt(LocalTime startAt);
}
