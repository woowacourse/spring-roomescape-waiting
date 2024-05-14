package roomescape.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    // Todo: limit(10개) 적용
    @Query("""
            select th as reservation_count from Theme th left outer join Reservation r on r.theme = th and r.date >= :start and r.date <= :end group by th.id, th.name, th.description, th.thumbnail order by count(r.id) desc
            """)
    List<Theme> findAllByDateBetweenAndOrderByReservationCount(@Param(value = "start") LocalDate startDate, @Param(value = "end") LocalDate endDate);
}
