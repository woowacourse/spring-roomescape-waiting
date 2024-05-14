package roomescape.time.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeRepository extends ListCrudRepository<ReservationTime, Long> {

    @Query(value = """
            SELECT id, start_at
            FROM reservation_time
            WHERE id IN (
                SELECT time_id
                FROM reservation
                WHERE date = :date AND theme_id = :themeId
            )
            """, nativeQuery = true)
    List<ReservationTime> findTimesExistsReservationDateAndThemeId(LocalDate date, Long themeId);
}
