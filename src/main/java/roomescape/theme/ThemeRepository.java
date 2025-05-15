package roomescape.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(nativeQuery = true,
            value = """
                            SELECT t.id, t.name, t.description, t.thumbnail
                            FROM theme t INNER JOIN reservation r
                                ON t.id=r.theme_id
                            WHERE r.date BETWEEN ?1 AND ?2
                            GROUP BY t.id, t.name, t.description, t.thumbnail
                            ORDER BY COUNT(*) DESC
                            LIMIT ?3
                    """)
    List<Theme> findAllOrderByRank(LocalDate from, LocalDate to, int size);
}
