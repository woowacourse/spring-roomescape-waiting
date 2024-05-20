package roomescape.reservation.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT th FROM Theme th 
            LEFT OUTER JOIN Reservation r 
                ON r.theme = th AND r.date >= :start AND r.date <= :end 
            GROUP BY th.id, th.name, th.description, th.thumbnail 
            ORDER BY count(r.id) DESC 
            """)
    List<Theme> findAllByDateBetweenOrderByReservationCount(@Param(value = "start") LocalDate startDate,
                                                            @Param(value = "end") LocalDate endDate,
                                                            Pageable pageable);
}
