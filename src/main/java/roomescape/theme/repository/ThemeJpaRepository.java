package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.domain.Theme;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {
    @Query(nativeQuery = true,
            value = """
                            SELECT t.id, t.name, t.description, t.thumbnail
                            FROM theme t INNER JOIN (
                                        SELECT r.theme_id AS theme_id, COUNT(*) AS count
                                        FROM reservation r
                                        WHERE r.date BETWEEN ?1 AND ?2
                                        GROUP BY r.theme_id
                                        ) AS r
                            ON t.id=r.theme_id
                            ORDER BY r.count DESC
                            LIMIT ?3
                    """)
    List<Theme> findAllOrderByRank(LocalDate from, LocalDate to, int size);

}
