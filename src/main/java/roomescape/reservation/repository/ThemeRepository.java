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
            select t.id, t.name, t.description, t.thumbnail, count(*) as cnt
            from theme t
            join reservation r
            on r.theme_id = t.id
            where r.date >= ?
            group by t.id
            order by cnt desc
            limit ?;
               """, nativeQuery = true)
    List<Theme> findPopularThemesDescOfLastWeekForLimit(LocalDate dateFrom, int limitCount);
}
