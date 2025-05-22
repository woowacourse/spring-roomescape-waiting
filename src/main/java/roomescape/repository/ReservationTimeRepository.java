package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.time.AvailableReservationTime;
import roomescape.domain.time.ReservationTime;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    @Query("""
            SELECT new roomescape.domain.time.AvailableReservationTime(
                rt,
                CASE WHEN COUNT(r.id) > 0 THEN true ELSE false END
            )
            FROM ReservationTime rt
            LEFT JOIN Reservation r
                ON r.reservationTime = rt AND r.reservationDate.date = :date AND r.theme.id = :themeId
            GROUP BY rt
            """)
    List<AvailableReservationTime> findAllAvailableReservationTimes(
            @Param("date") final LocalDate date,
            @Param("themeId") final Long themeId
    );

    boolean existsByStartAt(final LocalTime startAt);
}
