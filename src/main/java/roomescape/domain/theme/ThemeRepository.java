package roomescape.domain.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(ThemeName name);

    @Query(value = """
            SELECT t.id, name, description, thumbnail, COUNT(r.theme_id) AS reservation_count
            FROM theme t INNER JOIN reservation r
            ON r.theme_id = t.id
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.theme_id
            ORDER BY reservation_count DESC
            LIMIT :limit""", nativeQuery = true)
    List<Theme> findByReservationTermAndLimit(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate, @Param("limit") long limit);
}
