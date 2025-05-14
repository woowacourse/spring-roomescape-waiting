package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.entity.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAll();

    @Query("""
            SELECT t
            FROM Theme t
            LEFT JOIN Reservation r ON r.theme = t AND r.date BETWEEN :startDate AND :endDate
            GROUP BY t
            ORDER BY COUNT(r) DESC, t.id DESC
            LIMIT :limit
            """)
    List<Theme> findPopularDescendingUpTo(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("limit") int limit);

    Optional<Theme> findByName(String name);
}
