package roomescape.reservationtime.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeWebResponse;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime time);

    @Query("""
            SELECT new roomescape.reservationtime.presentation.dto.response.
            AvailableReservationTimeWebResponse(rt.id, rt.startAt, rs.id IS NOT NULL) 
            FROM ReservationTime AS rt 
            LEFT JOIN ReservationSlot rs ON rt.id = rs.time.id AND rs.date = :date AND rs.theme.id = :themeId 
            ORDER BY rt.startAt
            """)
    List<AvailableReservationTimeWebResponse> findAvailable(LocalDate date, Long themeId);
}
