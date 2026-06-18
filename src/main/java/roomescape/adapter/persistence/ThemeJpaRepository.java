package roomescape.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;
import roomescape.domain.repository.projection.PopularThemeProjection;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {

    // Reservation 이 엔티티가 되어 JPQL 집계로 승격(과도기 JdbcTemplate 제거). limit 은 Pageable 로.
    @Query("""
            select new roomescape.domain.repository.projection.PopularThemeProjection(t, count(r))
            from Theme t, Reservation r
            where r.theme = t and r.date >= :from and r.date < :to
            group by t
            order by count(r) desc
            """)
    List<PopularThemeProjection> findPopularBetween(@Param("from") LocalDate from,
                                                    @Param("to") LocalDate to,
                                                    Pageable pageable);
}
