package roomescape.domain.reservationtime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime getByIdentifier(Long id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 예약 시간이 존재하지 않습니다."));
    }

    @Query("""
            SELECT
                  new roomescape.domain.reservationtime.AvailableReservationTimeDto(
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

    boolean existsByStartAt(LocalTime startAt);
}
