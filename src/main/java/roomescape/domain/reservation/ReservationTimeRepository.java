package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.dto.AvailableReservationTimeDto;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);

    @Query("""
            SELECT
                  new roomescape.domain.reservation.dto.AvailableReservationTimeDto(
                      rt.id,
                      rt.startAt,
                      CASE WHEN COUNT(r.id) > 0 THEN true ELSE false END
                  )
            FROM ReservationTime rt
            LEFT JOIN Reservation r
            ON r.time.id = rt.id AND r.date = :date AND r.theme.id = :themeId
            GROUP BY rt.id, rt.startAt
            """)
    List<AvailableReservationTimeDto> findAvailableReservationTimes(
            @Param("date") LocalDate date,
            @Param("themeId") Long themeId
    );

    default ReservationTime getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new DomainNotFoundException("해당 id의 예약 시간이 존재하지 않습니다."));
    }
}
