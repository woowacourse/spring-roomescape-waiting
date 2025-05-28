package roomescape.theme.infrastructure;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT th FROM Theme th 
            left join ReservationSlot as rs
            on th.id = rs.theme.id 
            and rs.date between :fromDate and :toDate 
            group by th.id 
            order by count(rs.id) desc, 
            th.name asc 
            """)
    Page<Theme> findPopular(LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
