package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.model.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            select t
            from Theme as t
            left join Reservation r on r.theme.id = t.id
            group by t.id
            order by count(t.id) desc
            """)
    List<Theme> findAllOrderByReservationCount(Pageable pageable);
}
