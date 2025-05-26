package roomescape.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(value = """
                    SELECT t
                    FROM Theme t INNER JOIN Reservation r ON t.id = r.schedule.theme.id
                    WHERE r.schedule.date BETWEEN :from AND :to
                    GROUP BY t.id, t.name, t.description, t.thumbnail
                    ORDER BY COUNT(*) DESC
                    LIMIT :size
            """)
    List<Theme> findAllOrderByRank(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("size") int size);
}
