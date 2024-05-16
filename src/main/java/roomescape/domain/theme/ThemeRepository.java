package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(ThemeName name);

    /*    @Query(value = """
                SELECT id, name, description, thumbnail, reservation_count
                FROM theme INNER JOIN (
                    SELECT theme_id, COUNT(theme_id) AS reservation_count
                    FROM reservation r INNER JOIN schedule s ON r.schedule_id = s.id
                    WHERE r.date BETWEEN :startDate AND :endDate
                    GROUP BY theme_id
                ) ON theme_id = id
                ORDER BY reservation_count DESC
                LIMIT :limit""", nativeQuery = true)*/
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
