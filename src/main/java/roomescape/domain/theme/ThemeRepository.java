package roomescape.domain.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(ThemeName name);

    //TODO: 테스트 추가
    @Query(value = """
            SELECT t.id, t.name, t.description, t.thumbnail, COUNT(r.id) AS reservation_count
            FROM theme t
            INNER JOIN reservation_detail rd ON rd.theme_id = t.id
            INNER JOIN reservation r ON r.detail_id = rd.id
            WHERE rd.date BETWEEN :startDate AND :endDate
            AND r.status = 'RESERVED'
            GROUP BY t.id
            ORDER BY reservation_count DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Theme> findByReservationTermAndLimit(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("limit") long limit);
}
