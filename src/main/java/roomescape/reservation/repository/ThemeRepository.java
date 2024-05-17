package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Optional<Theme> findByThemeName_Name(String name);

    @Query("select t from Theme t join Reservation r on t.id = r.theme.id where t.id = :id")
    List<Theme> findThemesThatReservationReferById(Long id);

    @Query(value = """
            select t
            from Theme t
            join Reservation r
            on t.id = r.theme.id
            where r.date >= :dateFrom
            group by t.id
            order by count(r) desc
            limit :limitCount
               """)
    List<Theme> findPopularThemesDescOfLastWeekForLimit(LocalDate dateFrom, int limitCount);
}
