package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.model.theme.Name;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
                select r.reservationInfo.theme
                from Reservation r
                where r.reservationInfo.date between ?1 and ?2
                group by r.reservationInfo.theme
                order by count(r.reservationInfo.theme) desc, r.reservationInfo.theme asc
                limit ?3
                """)
    List<Theme> findRankingByDate(LocalDate startDate, LocalDate endDate, int rankingCount);

    boolean existsByName(Name name);
}
