package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface ReservationTimeRepository extends ListCrudRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime time);

    @Query("""
            select new roomescape.domain.reservation.TimeSlot(rt, (count(r.id) > 0))
            from ReservationTime as rt left join Reservation as r
            on rt = r.time and r.date = :date and r.theme.id = :themeId
            group by rt.id, rt.startAt
            """)
    List<TimeSlot> getReservationTimeAvailabilities(LocalDate date, long themeId);

    default ReservationTime getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약 시간입니다."));
    }
}
