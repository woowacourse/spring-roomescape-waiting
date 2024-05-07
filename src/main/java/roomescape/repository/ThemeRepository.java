package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.model.theme.Name;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
                select r.theme
                from Reservation r
                where r.date between ?1 and ?2
                group by r.theme
                order by count(r.theme) desc, r.theme asc
                limit ?3
                """)
    List<Theme> findRankingByDate(LocalDate startDate, LocalDate endDate, int rankingCount);

    boolean existsByName(Name name);
}
