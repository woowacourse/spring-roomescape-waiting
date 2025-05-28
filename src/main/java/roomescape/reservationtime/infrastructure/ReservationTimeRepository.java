package roomescape.reservationtime.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime time);

    @Query("""
            SELECT new roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse(rt.id, rt.startAt, 
            r.id IS NOT NULL) 
            FROM ReservationTime AS rt 
            LEFT JOIN ReservationSlot r ON rt.id = r.time.id AND r.date = :date AND r.theme.id = :themeId 
            ORDER BY rt.startAt
            """)
    List<AvailableReservationTimeResponse> findAvailable(LocalDate date, Long themeId);
}
