package roomescape.domain.theme.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.theme.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query(
            value = """
                    select
                        th.id as id,
                        th.name as name,
                        th.description as description,
                        th.thumbnail as thumbnail
                    from theme th
                    left join reservation r on th.id = r.theme_id
                    where r.date between dateadd('day', -7, current_date()) and dateadd('day', -1, current_date())
                    group by th.id, th.name, th.description, th.thumbnail
                    order by count(r.id) desc
                    limit 10;
                    """, nativeQuery = true
    )
    List<Theme> findThemeOrderByReservationCount();
}
