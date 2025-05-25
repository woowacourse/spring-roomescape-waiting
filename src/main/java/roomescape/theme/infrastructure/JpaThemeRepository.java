package roomescape.theme.infrastructure;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT th FROM Theme th 
            left join BookingSlot as bs
            on th.id = bs.theme.id 
            and bs.date between :fromDate and :toDate 
            group by th.id 
            order by count(bs.id) desc, 
            th.name asc 
            """)
    Page<Theme> findPopularThemes(LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
