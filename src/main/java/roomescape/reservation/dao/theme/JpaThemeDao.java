package roomescape.reservation.dao.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaThemeDao extends JpaRepository<Theme, Long> {

    @Query(value = """
        SELECT t.*
        FROM reservation r
            JOIN theme t ON r.theme_id = t.id
        WHERE r.date BETWEEN :startDate AND :endDate
        GROUP BY t.id
        ORDER BY COUNT(r.id) DESC
        LIMIT :limitCount
    """, nativeQuery = true)
    List<Theme> findMostReservedThemesBetweenLimit(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("limitCount") int limitCount);

    boolean existsByName(String name);

    int countById(Long id);
}
